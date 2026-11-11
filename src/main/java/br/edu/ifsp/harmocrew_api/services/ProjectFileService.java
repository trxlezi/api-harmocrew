package br.edu.ifsp.harmocrew_api.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import br.edu.ifsp.harmocrew_api.dtos.FileDtos.FileVersionResponse;
import br.edu.ifsp.harmocrew_api.dtos.FileDtos.ProjectFileResponse;
import br.edu.ifsp.harmocrew_api.entities.Artist;
import br.edu.ifsp.harmocrew_api.entities.FileVersion;
import br.edu.ifsp.harmocrew_api.entities.MusicalProject;
import br.edu.ifsp.harmocrew_api.entities.ProjectFile;
import br.edu.ifsp.harmocrew_api.enums.FileCategory;
import br.edu.ifsp.harmocrew_api.enums.NotificationType;
import br.edu.ifsp.harmocrew_api.exceptions.BusinessException;
import br.edu.ifsp.harmocrew_api.exceptions.ResourceNotFoundException;
import br.edu.ifsp.harmocrew_api.repositories.FileVersionRepository;
import br.edu.ifsp.harmocrew_api.repositories.ProjectFileRepository;

/*
 * Versionamento automatico de arquivos do projeto (Funcionalidade 2).
 *
 * - Todo upload gera uma nova versao numerada com autor, data e hash.
 * - Versoes nunca sao sobrescritas nem apagadas: cada uma tem link imutavel.
 * - Arquivo travado (decidido em enquete) nao aceita novas versoes ate ser
 *   destravado, e o destravamento fica registrado no log de auditoria.
 */
@Service
public class ProjectFileService {

	private final ProjectFileRepository fileRepository;
	private final FileVersionRepository versionRepository;
	private final MusicalProjectService projectService;
	private final StorageService storageService;
	private final AuditLogService auditLogService;
	private final NotificationService notificationService;
	private final CurrentArtistResolver currentArtist;

	public ProjectFileService(
		ProjectFileRepository fileRepository,
		FileVersionRepository versionRepository,
		MusicalProjectService projectService,
		StorageService storageService,
		AuditLogService auditLogService,
		NotificationService notificationService,
		CurrentArtistResolver currentArtist
	) {
		this.fileRepository = fileRepository;
		this.versionRepository = versionRepository;
		this.projectService = projectService;
		this.storageService = storageService;
		this.auditLogService = auditLogService;
		this.notificationService = notificationService;
		this.currentArtist = currentArtist;
	}

	@Transactional(readOnly = true)
	public List<ProjectFileResponse> list(Long projectId, FileCategory category) {
		return fileRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
			.filter(file -> category == null || file.getCategory() == category)
			.map(ProjectFileService::toResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public ProjectFileResponse get(Long id) {
		return toResponse(findEntity(id));
	}

	@Transactional
	public ProjectFileResponse create(Long projectId, MultipartFile upload, String name, FileCategory category,
		String description, String note, Long artistId) {
		MusicalProject project = projectService.findEntity(projectId);
		Artist actor = currentArtist.resolve(artistId);
		ProjectFile file = new ProjectFile();
		file.setProject(project);
		file.setName(name == null || name.isBlank() ? upload.getOriginalFilename() : name.trim());
		if (file.getName() == null || file.getName().isBlank()) {
			throw new BusinessException("Informe o nome do arquivo");
		}
		file.setCategory(category == null ? FileCategory.OTHER : category);
		file.setDescription(description);
		file.setCreatedBy(actor);
		fileRepository.save(file);
		FileVersion version = addVersion(file, upload, note, actor);
		auditLogService.record(projectId, "FILE_CREATED", "ProjectFile", file.getId(), actor,
			"Arquivo \"" + file.getName() + "\" criado (v" + version.getVersionNumber() + ")");
		return toResponse(file);
	}

	@Transactional
	public ProjectFileResponse uploadVersion(Long fileId, MultipartFile upload, String note, Long artistId) {
		ProjectFile file = findEntity(fileId);
		if (file.isLocked()) {
			throw new BusinessException("Arquivo travado na versao decidida (v"
				+ file.getLockedVersion().getVersionNumber() + "). Destrave antes de enviar nova versao.");
		}
		Artist actor = currentArtist.resolve(artistId);
		FileVersion version = addVersion(file, upload, note, actor);
		Long projectId = file.getProject().getId();
		auditLogService.record(projectId, "FILE_VERSION_UPLOADED", "ProjectFile", file.getId(), actor,
			"\"" + file.getName() + "\" v" + version.getVersionNumber() + (note == null || note.isBlank() ? "" : ": " + note));
		notificationService.notifyAll(file.getProject().getArtists(), actor == null ? null : actor.getId(), projectId,
			NotificationType.FILE_VERSION_UPLOADED, "Nova versao: " + file.getName(),
			"v" + version.getVersionNumber() + " enviada" + (actor == null ? "" : " por " + actor.getStageName()), file.getId());
		return toResponse(file);
	}

	@Transactional(readOnly = true)
	public FileVersionResponse getVersion(Long fileId, int versionNumber) {
		ProjectFile file = findEntity(fileId);
		return file.getVersions().stream()
			.filter(version -> version.getVersionNumber() == versionNumber)
			.findFirst()
			.map(version -> toVersionResponse(version, file))
			.orElseThrow(() -> new ResourceNotFoundException("Versao " + versionNumber + " nao encontrada"));
	}

	// Chamado pelo encerramento da enquete: trava o arquivo na versao vencedora.
	@Transactional
	public void lockAt(FileVersion version, Long pollId, Artist actor) {
		ProjectFile file = version.getFile();
		file.setLocked(true);
		file.setLockedVersion(version);
		file.setLockedByPollId(pollId);
		file.setLockedAt(LocalDateTime.now());
		auditLogService.record(file.getProject().getId(), "FILE_LOCKED", "ProjectFile", file.getId(), actor,
			"\"" + file.getName() + "\" travado na v" + version.getVersionNumber() + " (enquete " + pollId + ")");
	}

	@Transactional
	public ProjectFileResponse unlock(Long fileId, Long artistId, String reason) {
		ProjectFile file = findEntity(fileId);
		if (!file.isLocked()) {
			throw new BusinessException("Arquivo nao esta travado");
		}
		if (reason == null || reason.isBlank()) {
			throw new BusinessException("Informe o motivo para destravar a versao decidida");
		}
		Artist actor = currentArtist.resolve(artistId);
		int lockedNumber = file.getLockedVersion().getVersionNumber();
		file.setLocked(false);
		file.setLockedVersion(null);
		file.setLockedByPollId(null);
		file.setLockedAt(null);
		auditLogService.record(file.getProject().getId(), "FILE_UNLOCKED", "ProjectFile", file.getId(), actor,
			"\"" + file.getName() + "\" destravado (estava na v" + lockedNumber + "). Motivo: " + reason.trim());
		return toResponse(file);
	}

	FileVersion findVersion(Long versionId) {
		return versionRepository.findById(versionId)
			.orElseThrow(() -> new ResourceNotFoundException("Versao de arquivo nao encontrada: " + versionId));
	}

	private ProjectFile findEntity(Long id) {
		return fileRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Arquivo nao encontrado: " + id));
	}

	private FileVersion addVersion(ProjectFile file, MultipartFile upload, String note, Artist actor) {
		StorageService.StoredFile stored = storageService.store(upload);
		FileVersion latest = file.latestVersion();
		if (latest != null && latest.getSha256().equals(stored.sha256())) {
			throw new BusinessException("Conteudo identico a versao atual (v" + latest.getVersionNumber() + ")");
		}
		FileVersion version = new FileVersion();
		version.setFile(file);
		version.setVersionNumber(latest == null ? 1 : latest.getVersionNumber() + 1);
		version.setStorageKey(stored.key());
		version.setOriginalFilename(stored.originalFilename());
		version.setContentType(stored.contentType());
		version.setSizeBytes(stored.size());
		version.setSha256(stored.sha256());
		version.setNote(note == null || note.isBlank() ? null : note.trim());
		version.setUploadedBy(actor);
		file.getVersions().add(version);
		return versionRepository.save(version);
	}

	static ProjectFileResponse toResponse(ProjectFile file) {
		FileVersion locked = file.getLockedVersion();
		// Versao vigente: a decidida (se travado) ou a mais recente.
		FileVersion current = locked != null ? locked : file.latestVersion();
		return new ProjectFileResponse(
			file.getId(),
			file.getProject().getId(),
			file.getName(),
			file.getCategory(),
			file.getDescription(),
			file.getCreatedAt(),
			file.getCreatedBy() == null ? null : file.getCreatedBy().getId(),
			file.getCreatedBy() == null ? null : file.getCreatedBy().getStageName(),
			file.isLocked(),
			locked == null ? null : locked.getId(),
			locked == null ? null : locked.getVersionNumber(),
			file.getLockedByPollId(),
			file.getLockedAt(),
			current == null ? null : toVersionResponse(current, file),
			file.getVersions().stream().map(version -> toVersionResponse(version, file)).toList().reversed());
	}

	static FileVersionResponse toVersionResponse(FileVersion version, ProjectFile file) {
		Artist author = version.getUploadedBy();
		boolean decided = file.getLockedVersion() != null && file.getLockedVersion().getId().equals(version.getId());
		return new FileVersionResponse(
			version.getId(),
			file.getId(),
			version.getVersionNumber(),
			StorageService.MEDIA_PREFIX + version.getStorageKey(),
			version.getOriginalFilename(),
			version.getContentType(),
			version.getSizeBytes(),
			version.getSha256(),
			version.getNote(),
			author == null ? null : author.getId(),
			author == null ? null : author.getStageName(),
			version.getUploadedAt(),
			decided);
	}
}
