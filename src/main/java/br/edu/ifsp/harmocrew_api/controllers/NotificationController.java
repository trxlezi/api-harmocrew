package br.edu.ifsp.harmocrew_api.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import br.edu.ifsp.harmocrew_api.dtos.NotificationResponse;
import br.edu.ifsp.harmocrew_api.entities.Artist;
import br.edu.ifsp.harmocrew_api.exceptions.BusinessException;
import br.edu.ifsp.harmocrew_api.services.CurrentArtistResolver;
import br.edu.ifsp.harmocrew_api.services.NotificationService;

/*
 * Alertas em tempo real (Funcionalidade 3): prazos estourados, mudanca de
 * status, novo responsavel, enquetes e retorno de candidaturas.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationService notificationService;
	private final CurrentArtistResolver currentArtist;

	public NotificationController(NotificationService notificationService, CurrentArtistResolver currentArtist) {
		this.notificationService = notificationService;
		this.currentArtist = currentArtist;
	}

	@GetMapping
	public List<NotificationResponse> list(
		@RequestParam(required = false) Long artistId,
		@RequestParam(defaultValue = "false") boolean unreadOnly
	) {
		return notificationService.list(requireArtist(artistId), unreadOnly);
	}

	// Stream SSE: eventos "ready" (contador de nao lidas) e "notification".
	@GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter stream(@RequestParam(required = false) Long artistId) {
		return notificationService.subscribe(requireArtist(artistId));
	}

	@PatchMapping("/{id}/read")
	public NotificationResponse markRead(@PathVariable Long id) {
		return notificationService.markRead(id);
	}

	@PatchMapping("/read-all")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void markAllRead(@RequestParam(required = false) Long artistId) {
		notificationService.markAllRead(requireArtist(artistId));
	}

	private Long requireArtist(Long artistId) {
		Artist artist = currentArtist.resolve(artistId);
		if (artist == null) {
			throw new BusinessException("Usuario sem perfil de artista");
		}
		return artist.getId();
	}
}
