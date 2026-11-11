package br.edu.ifsp.harmocrew_api.controllers;

import java.time.Duration;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifsp.harmocrew_api.services.StorageService;

/*
 * Links imutaveis de arquivos. A chave e um UUID impossivel de adivinhar e
 * nunca e sobrescrita, por isso a resposta pode ser cacheada para sempre.
 */
@RestController
public class MediaController {

	private final StorageService storageService;

	public MediaController(StorageService storageService) {
		this.storageService = storageService;
	}

	@GetMapping("/media/{key}")
	public ResponseEntity<Resource> get(@PathVariable String key) {
		Resource resource = storageService.load(key);
		return ResponseEntity.ok()
			.cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
			.contentType(MediaType.parseMediaType(storageService.probeContentType(key)))
			.body(resource);
	}
}
