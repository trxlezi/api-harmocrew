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

import br.edu.ifsp.harmocrew_api.dtos.DecisionRecordRequest;
import br.edu.ifsp.harmocrew_api.dtos.DecisionRecordResponse;
import br.edu.ifsp.harmocrew_api.dtos.DecisionStatusUpdateRequest;
import br.edu.ifsp.harmocrew_api.services.DecisionRecordService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class DecisionRecordController {

	private final DecisionRecordService decisionService;

	public DecisionRecordController(DecisionRecordService decisionService) {
		this.decisionService = decisionService;
	}

	@GetMapping("/decisions")
	public List<DecisionRecordResponse> list(@RequestParam(required = false) Long projectId) {
		return decisionService.list(projectId);
	}

	@GetMapping("/projects/{projectId}/decisions")
	public List<DecisionRecordResponse> listByProject(@PathVariable Long projectId) {
		return decisionService.list(projectId);
	}

	@PostMapping("/projects/{projectId}/decisions")
	@ResponseStatus(HttpStatus.CREATED)
	public DecisionRecordResponse create(@PathVariable Long projectId, @Valid @RequestBody DecisionRecordRequest request) {
		return decisionService.create(projectId, request);
	}

	@PutMapping("/decisions/{id}")
	public DecisionRecordResponse update(@PathVariable Long id, @Valid @RequestBody DecisionRecordRequest request) {
		return decisionService.update(id, request);
	}

	@PatchMapping("/decisions/{id}/status")
	public DecisionRecordResponse updateStatus(@PathVariable Long id, @Valid @RequestBody DecisionStatusUpdateRequest request) {
		return decisionService.updateStatus(id, request);
	}

	@DeleteMapping("/decisions/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		decisionService.delete(id);
	}
}
