package br.edu.ifsp.harmocrew_api.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.edu.ifsp.harmocrew_api.dtos.ArtistRequest;
import br.edu.ifsp.harmocrew_api.dtos.ArtistResponse;
import br.edu.ifsp.harmocrew_api.exceptions.BusinessException;
import br.edu.ifsp.harmocrew_api.services.ArtistService;
import br.edu.ifsp.harmocrew_api.services.StorageService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/artists")
public class ArtistController {

	private final ArtistService artistService;
	private final StorageService storageService;

	public ArtistController(ArtistService artistService, StorageService storageService) {
		this.artistService = artistService;
		this.storageService = storageService;
	}

	@GetMapping
	public List<ArtistResponse> list(
		@RequestParam(required = false) String style,
		@RequestParam(required = false) String instrument,
		@RequestParam(required = false) String city
	) {
		if (style == null && instrument == null && city == null) {
			return artistService.list();
		}
		return artistService.search(style, instrument, city);
	}

	/*
	 * Upload do audio de apresentacao do perfil (multipart, campo "file").
	 * O audio fica disponivel em um link imutavel /media/{chave}.
	 */
	@PostMapping(path = "/{id}/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ArtistResponse uploadAudio(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
		String type = file.getContentType();
		if (type != null && !type.startsWith("audio/") && !type.equals("application/octet-stream")) {
			throw new BusinessException("Envie um arquivo de audio");
		}
		return artistService.attachAudio(id, storageService.store(file));
	}

	@GetMapping("/{id}")
	public ArtistResponse get(@PathVariable Long id) {
		return artistService.get(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ArtistResponse create(@Valid @RequestBody ArtistRequest request) {
		return artistService.create(request);
	}

	@PutMapping("/{id}")
	public ArtistResponse update(@PathVariable Long id, @Valid @RequestBody ArtistRequest request) {
		return artistService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		artistService.delete(id);
	}
}
