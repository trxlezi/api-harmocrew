package br.edu.ifsp.harmocrew_api.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.edu.ifsp.harmocrew_api.exceptions.BusinessException;
import br.edu.ifsp.harmocrew_api.exceptions.ResourceNotFoundException;

/*
 * Armazena arquivos enviados (guias de audio, arranjos, PDFs, audio do perfil)
 * no disco. Cada arquivo recebe uma chave UUID que nunca e reaproveitada nem
 * sobrescrita: o link /media/{chave} e imutavel.
 */
@Service
public class StorageService {

	public static final String MEDIA_PREFIX = "/media/";
	private static final Pattern EXTENSION = Pattern.compile("\\.[a-z0-9]{1,8}");
	private static final Pattern KEY = Pattern.compile("[0-9a-f-]{36}(\\.[a-z0-9]{1,8})?");

	private final Path root;

	public StorageService(@Value("${app.storage.dir:uploads}") String dir) {
		this.root = Path.of(dir).toAbsolutePath().normalize();
	}

	public record StoredFile(String key, String originalFilename, String contentType, long size, String sha256) {
		public String url() {
			return MEDIA_PREFIX + key;
		}
	}

	public StoredFile store(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BusinessException("Arquivo vazio ou ausente");
		}
		String original = file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()
			? "arquivo"
			: Path.of(file.getOriginalFilename().replace('\\', '/')).getFileName().toString();
		String extension = original.contains(".") ? original.substring(original.lastIndexOf('.')).toLowerCase() : "";
		if (!EXTENSION.matcher(extension).matches()) {
			extension = "";
		}
		String key = UUID.randomUUID() + extension;
		try {
			Files.createDirectories(root);
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			try (InputStream in = new DigestInputStream(file.getInputStream(), digest)) {
				Files.copy(in, root.resolve(key));
			}
			String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
			return new StoredFile(key, original, contentType, file.getSize(), HexFormat.of().formatHex(digest.digest()));
		}
		catch (IOException | NoSuchAlgorithmException exception) {
			throw new BusinessException("Falha ao salvar arquivo: " + exception.getMessage());
		}
	}

	public Resource load(String key) {
		if (key == null || !KEY.matcher(key).matches()) {
			throw new ResourceNotFoundException("Arquivo nao encontrado");
		}
		Path path = root.resolve(key).normalize();
		if (!path.startsWith(root) || !Files.exists(path)) {
			throw new ResourceNotFoundException("Arquivo nao encontrado");
		}
		return new FileSystemResource(path);
	}

	public String probeContentType(String key) {
		try {
			String type = Files.probeContentType(root.resolve(key));
			return type == null ? "application/octet-stream" : type;
		}
		catch (IOException exception) {
			return "application/octet-stream";
		}
	}
}
