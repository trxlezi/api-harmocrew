package br.edu.ifsp.harmocrew_api.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifsp.harmocrew_api.dtos.PollDtos.ClosePollRequest;
import br.edu.ifsp.harmocrew_api.dtos.PollDtos.PollRequest;
import br.edu.ifsp.harmocrew_api.dtos.PollDtos.PollResponse;
import br.edu.ifsp.harmocrew_api.dtos.PollDtos.VoteRequest;
import br.edu.ifsp.harmocrew_api.services.PollService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class PollController {

	private final PollService pollService;

	public PollController(PollService pollService) {
		this.pollService = pollService;
	}

	@GetMapping("/projects/{projectId}/polls")
	public List<PollResponse> list(@PathVariable Long projectId) {
		return pollService.list(projectId);
	}

	@PostMapping("/projects/{projectId}/polls")
	@ResponseStatus(HttpStatus.CREATED)
	public PollResponse create(@PathVariable Long projectId, @Valid @RequestBody PollRequest request) {
		return pollService.create(projectId, request);
	}

	@GetMapping("/polls/{id}")
	public PollResponse get(@PathVariable Long id) {
		return pollService.get(id);
	}

	@PostMapping("/polls/{id}/votes")
	public PollResponse vote(@PathVariable Long id, @Valid @RequestBody VoteRequest request) {
		return pollService.vote(id, request);
	}

	@PostMapping("/polls/{id}/close")
	public PollResponse close(@PathVariable Long id, @RequestBody(required = false) ClosePollRequest request) {
		return pollService.close(id, request);
	}
}
