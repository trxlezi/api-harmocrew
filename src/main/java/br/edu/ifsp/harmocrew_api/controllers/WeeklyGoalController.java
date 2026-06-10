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

import br.edu.ifsp.harmocrew_api.dtos.WeeklyGoalRequest;
import br.edu.ifsp.harmocrew_api.dtos.WeeklyGoalResponse;
import br.edu.ifsp.harmocrew_api.dtos.WeeklyGoalStatusUpdateRequest;
import br.edu.ifsp.harmocrew_api.services.WeeklyGoalService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class WeeklyGoalController {

	private final WeeklyGoalService goalService;

	public WeeklyGoalController(WeeklyGoalService goalService) {
		this.goalService = goalService;
	}

	@GetMapping("/weekly-goals")
	public List<WeeklyGoalResponse> list(
		@RequestParam(required = false) Long projectId,
		@RequestParam(required = false) Long ownerArtistId
	) {
		return goalService.list(projectId, ownerArtistId);
	}

	@GetMapping("/projects/{projectId}/weekly-goals")
	public List<WeeklyGoalResponse> listByProject(@PathVariable Long projectId) {
		return goalService.list(projectId, null);
	}

	@PostMapping("/projects/{projectId}/weekly-goals")
	@ResponseStatus(HttpStatus.CREATED)
	public WeeklyGoalResponse create(@PathVariable Long projectId, @Valid @RequestBody WeeklyGoalRequest request) {
		return goalService.create(projectId, request);
	}

	@PutMapping("/weekly-goals/{id}")
	public WeeklyGoalResponse update(@PathVariable Long id, @Valid @RequestBody WeeklyGoalRequest request) {
		return goalService.update(id, request);
	}

	@PatchMapping("/weekly-goals/{id}/status")
	public WeeklyGoalResponse updateStatus(
		@PathVariable Long id,
		@Valid @RequestBody WeeklyGoalStatusUpdateRequest request
	) {
		return goalService.updateStatus(id, request);
	}

	@DeleteMapping("/weekly-goals/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		goalService.delete(id);
	}
}
