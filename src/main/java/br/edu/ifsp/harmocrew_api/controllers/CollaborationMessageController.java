package br.edu.ifsp.harmocrew_api.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifsp.harmocrew_api.dtos.CollaborationMessageRequest;
import br.edu.ifsp.harmocrew_api.dtos.CollaborationMessageResponse;
import br.edu.ifsp.harmocrew_api.services.CollaborationMessageService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class CollaborationMessageController {

	private final CollaborationMessageService messageService;

	public CollaborationMessageController(CollaborationMessageService messageService) {
		this.messageService = messageService;
	}

	@GetMapping("/messages")
	public List<CollaborationMessageResponse> list(@RequestParam(required = false) Long projectId) {
		return messageService.list(projectId);
	}

	@GetMapping("/projects/{projectId}/messages")
	public List<CollaborationMessageResponse> listByProject(@PathVariable Long projectId) {
		return messageService.list(projectId);
	}

	@PostMapping("/projects/{projectId}/messages")
	@ResponseStatus(HttpStatus.CREATED)
	public CollaborationMessageResponse create(
		@PathVariable Long projectId,
		@Valid @RequestBody CollaborationMessageRequest request
	) {
		return messageService.create(projectId, request);
	}

	@DeleteMapping("/messages/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		messageService.delete(id);
	}
}
