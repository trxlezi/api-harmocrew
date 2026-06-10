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

import br.edu.ifsp.harmocrew_api.dtos.MusicalProjectRequest;
import br.edu.ifsp.harmocrew_api.dtos.MusicalProjectResponse;
import br.edu.ifsp.harmocrew_api.dtos.MusicalProjectSummaryResponse;
import br.edu.ifsp.harmocrew_api.services.MusicalProjectService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects")
public class MusicalProjectController {

	private final MusicalProjectService projectService;

	public MusicalProjectController(MusicalProjectService projectService) {
		this.projectService = projectService;
	}

	@GetMapping
	public List<MusicalProjectResponse> list() {
		return projectService.list();
	}

	@GetMapping("/summaries")
	public List<MusicalProjectSummaryResponse> listSummaries() {
		return projectService.listSummaries();
	}

	@GetMapping("/{id}")
	public MusicalProjectResponse get(@PathVariable Long id) {
		return projectService.get(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public MusicalProjectResponse create(@Valid @RequestBody MusicalProjectRequest request) {
		return projectService.create(request);
	}

	@PutMapping("/{id}")
	public MusicalProjectResponse update(@PathVariable Long id, @Valid @RequestBody MusicalProjectRequest request) {
		return projectService.update(id, request);
	}

	@PostMapping("/{projectId}/artists/{artistId}")
	public MusicalProjectResponse addArtist(@PathVariable Long projectId, @PathVariable Long artistId) {
		return projectService.addArtist(projectId, artistId);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		projectService.delete(id);
	}
}
