package br.edu.ifsp.harmocrew_api.dtos;

import java.time.LocalDateTime;

public record AuditLogResponse(
	Long id,
	Long projectId,
	String action,
	String entityType,
	Long entityId,
	Long actorArtistId,
	String actorName,
	String details,
	LocalDateTime createdAt
) {
}
