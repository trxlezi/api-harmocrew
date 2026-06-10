package br.edu.ifsp.harmocrew_api.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifsp.harmocrew_api.dtos.RehearsalRequest;
import br.edu.ifsp.harmocrew_api.dtos.RehearsalResponse;
import br.edu.ifsp.harmocrew_api.dtos.RehearsalStatusUpdateRequest;
import br.edu.ifsp.harmocrew_api.services.RehearsalService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class RehearsalController {

	private final RehearsalService rehearsalService;

	public RehearsalController(RehearsalService rehearsalService) {
		this.rehearsalService = rehearsalService;
	}

	@GetMapping("/rehearsals")
	public List<RehearsalResponse> list(@RequestParam(required = false) Long projectId) {
		return rehearsalService.list(projectId);
	}

	@GetMapping("/projects/{projectId}/rehearsals")
	public List<RehearsalResponse> listByProject(@PathVariable Long projectId) {
		return rehearsalService.list(projectId);
	}

	@PostMapping("/projects/{projectId}/rehearsals")
	@ResponseStatus(HttpStatus.CREATED)
	public RehearsalResponse create(@PathVariable Long projectId, @Valid @RequestBody RehearsalRequest request) {
		return rehearsalService.create(projectId, request);
	}

	@PutMapping("/rehearsals/{id}")
	public RehearsalResponse update(@PathVariable Long id, @Valid @RequestBody RehearsalRequest request) {
		return rehearsalService.update(id, request);
	}

	@PatchMapping("/rehearsals/{id}/status")
	public RehearsalResponse updateStatus(@PathVariable Long id, @Valid @RequestBody RehearsalStatusUpdateRequest request) {
		return rehearsalService.updateStatus(id, request);
	}

	@DeleteMapping("/rehearsals/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		rehearsalService.delete(id);
	}
}
