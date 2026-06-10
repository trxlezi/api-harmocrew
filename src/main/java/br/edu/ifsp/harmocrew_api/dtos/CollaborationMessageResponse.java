package br.edu.ifsp.harmocrew_api.dtos;

import java.time.LocalDateTime;

import br.edu.ifsp.harmocrew_api.enums.CollaborationMessageType;

public record CollaborationMessageResponse(
	Long id,
	Long projectId,
	String projectTitle,
	Long senderArtistId,
	String senderArtistName,
	String content,
	LocalDateTime sentAt,
	CollaborationMessageType type
) {
}
