package br.edu.ifsp.harmocrew_api.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifsp.harmocrew_api.dtos.ArtistRequest;
import br.edu.ifsp.harmocrew_api.dtos.ArtistResponse;
import br.edu.ifsp.harmocrew_api.services.ArtistService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/artists")
public class ArtistController {

	private final ArtistService artistService;

	public ArtistController(ArtistService artistService) {
		this.artistService = artistService;
	}

	@GetMapping
	public List<ArtistResponse> list() {
		return artistService.list();
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
