package br.edu.ifsp.harmocrew_api.dtos;

import java.time.LocalDateTime;

import br.edu.ifsp.harmocrew_api.enums.NotificationType;

public record NotificationResponse(
	Long id,
	Long recipientArtistId,
	Long projectId,
	NotificationType type,
	String title,
	String body,
	Long referenceId,
	boolean read,
	LocalDateTime createdAt
) {
}
