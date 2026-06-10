package br.edu.ifsp.harmocrew_api.dtos;

import java.time.LocalDate;

import br.edu.ifsp.harmocrew_api.enums.DecisionStatus;

public record DecisionRecordResponse(
	Long id,
	Long projectId,
	String projectTitle,
	String title,
	String description,
	Long decidedByArtistId,
	String decidedByArtistName,
	LocalDate decidedAt,
	String impact,
	DecisionStatus status
) {
}
