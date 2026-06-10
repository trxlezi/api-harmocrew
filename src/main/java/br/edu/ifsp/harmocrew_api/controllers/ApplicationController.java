package br.edu.ifsp.harmocrew_api.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifsp.harmocrew_api.dtos.ApplicationRequest;
import br.edu.ifsp.harmocrew_api.dtos.ApplicationResponse;
import br.edu.ifsp.harmocrew_api.dtos.ApplicationStatusUpdateRequest;
import br.edu.ifsp.harmocrew_api.services.ApplicationService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class ApplicationController {

	private final ApplicationService applicationService;

	public ApplicationController(ApplicationService applicationService) {
		this.applicationService = applicationService;
	}

	@GetMapping("/applications")
	public List<ApplicationResponse> list(
		@RequestParam(required = false) Long projectId,
		@RequestParam(required = false) Long artistId
	) {
		return applicationService.list(projectId, artistId);
	}

	@GetMapping("/applications/{id}")
	public ApplicationResponse get(@PathVariable Long id) {
		return applicationService.get(id);
	}

	@GetMapping("/projects/{projectId}/applications")
	public List<ApplicationResponse> listByProject(@PathVariable Long projectId) {
		return applicationService.list(projectId, null);
	}

	@PostMapping("/projects/{projectId}/applications")
	@ResponseStatus(HttpStatus.CREATED)
	public ApplicationResponse create(@PathVariable Long projectId, @Valid @RequestBody ApplicationRequest request) {
		return applicationService.create(projectId, request);
	}

	@PatchMapping("/applications/{id}/status")
	public ApplicationResponse updateStatus(
		@PathVariable Long id,
		@Valid @RequestBody ApplicationStatusUpdateRequest request
	) {
		return applicationService.updateStatus(id, request);
	}

	@DeleteMapping("/applications/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		applicationService.delete(id);
	}
}
