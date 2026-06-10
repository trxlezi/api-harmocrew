package br.edu.ifsp.harmocrew_api.dtos;

import java.time.LocalDateTime;

import br.edu.ifsp.harmocrew_api.enums.ApplicationStatus;

public record ApplicationResponse(
	Long id,
	String message,
	String specialty,
	String availability,
	ApplicationStatus status,
	LocalDateTime createdAt,
	Long artistId,
	String artistName,
	Long projectId,
	String projectTitle
) {
}
