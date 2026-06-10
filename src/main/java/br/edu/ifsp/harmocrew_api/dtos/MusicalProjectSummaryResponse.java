package br.edu.ifsp.harmocrew_api.dtos;

import br.edu.ifsp.harmocrew_api.enums.ProjectStatus;

public record MusicalProjectSummaryResponse(
	Long id,
	String title,
	String musicalStyle,
	ProjectStatus status
) {
}
