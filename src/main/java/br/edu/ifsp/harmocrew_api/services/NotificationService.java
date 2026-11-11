package br.edu.ifsp.harmocrew_api.services;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import br.edu.ifsp.harmocrew_api.dtos.NotificationResponse;
import br.edu.ifsp.harmocrew_api.entities.Artist;
import br.edu.ifsp.harmocrew_api.entities.Notification;
import br.edu.ifsp.harmocrew_api.enums.NotificationType;
import br.edu.ifsp.harmocrew_api.exceptions.ResourceNotFoundException;
import br.edu.ifsp.harmocrew_api.repositories.NotificationRepository;

/*
 * Notificacoes persistidas + entrega em tempo real via Server-Sent Events.
 * O app abre GET /api/notifications/stream e recebe cada alerta assim que a
 * transacao que o gerou e confirmada.
 */
@Service
public class NotificationService {

	private final NotificationRepository repository;
	private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

	public NotificationService(NotificationRepository repository) {
		this.repository = repository;
	}

	public SseEmitter subscribe(Long artistId) {
		SseEmitter emitter = new SseEmitter(0L);
		List<SseEmitter> list = emitters.computeIfAbsent(artistId, id -> new CopyOnWriteArrayList<>());
		list.add(emitter);
		Runnable remove = () -> list.remove(emitter);
		emitter.onCompletion(remove);
		emitter.onTimeout(remove);
		emitter.onError(error -> remove.run());
		try {
			emitter.send(SseEmitter.event().name("ready")
				.data(Map.of("unread", repository.countByRecipientArtistIdAndReadFalse(artistId))));
		}
		catch (IOException exception) {
			remove.run();
		}
		return emitter;
	}

	@Transactional
	public void notify(Long recipientArtistId, Long projectId, NotificationType type, String title, String body,
		Long referenceId) {
		if (recipientArtistId == null) {
			return;
		}
		Notification notification = new Notification();
		notification.setRecipientArtistId(recipientArtistId);
		notification.setProjectId(projectId);
		notification.setType(type);
		notification.setTitle(title);
		notification.setBody(body);
		notification.setReferenceId(referenceId);
		NotificationResponse response = toResponse(repository.save(notification));
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					push(response);
				}
			});
		}
		else {
			push(response);
		}
	}

	@Transactional
	public void notifyAll(Collection<Artist> recipients, Long excludeArtistId, Long projectId, NotificationType type,
		String title, String body, Long referenceId) {
		recipients.stream()
			.map(Artist::getId)
			.filter(Objects::nonNull)
			.filter(id -> !id.equals(excludeArtistId))
			.distinct()
			.forEach(id -> notify(id, projectId, type, title, body, referenceId));
	}

	@Transactional(readOnly = true)
	public List<NotificationResponse> list(Long artistId, boolean unreadOnly) {
		List<Notification> list = unreadOnly
			? repository.findByRecipientArtistIdAndReadFalseOrderByCreatedAtDescIdDesc(artistId)
			: repository.findByRecipientArtistIdOrderByCreatedAtDescIdDesc(artistId);
		return list.stream().map(NotificationService::toResponse).toList();
	}

	@Transactional
	public NotificationResponse markRead(Long id) {
		Notification notification = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Notificacao nao encontrada: " + id));
		notification.setRead(true);
		return toResponse(notification);
	}

	@Transactional
	public void markAllRead(Long artistId) {
		repository.findByRecipientArtistIdAndReadFalseOrderByCreatedAtDescIdDesc(artistId)
			.forEach(notification -> notification.setRead(true));
	}

	private void push(NotificationResponse response) {
		List<SseEmitter> list = emitters.get(response.recipientArtistId());
		if (list == null) {
			return;
		}
		for (SseEmitter emitter : list) {
			try {
				emitter.send(SseEmitter.event().name("notification").data(response));
			}
			catch (IOException | IllegalStateException exception) {
				list.remove(emitter);
			}
		}
	}

	static NotificationResponse toResponse(Notification n) {
		return new NotificationResponse(n.getId(), n.getRecipientArtistId(), n.getProjectId(), n.getType(),
			n.getTitle(), n.getBody(), n.getReferenceId(), n.isRead(), n.getCreatedAt());
	}
}
