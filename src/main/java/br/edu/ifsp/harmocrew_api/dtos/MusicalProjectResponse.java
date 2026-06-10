package br.edu.ifsp.harmocrew_api.dtos;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import br.edu.ifsp.harmocrew_api.enums.ProjectStatus;

public record MusicalProjectResponse(
	Long id,
	String title,
	String description,
	String musicalStyle,
	ProjectStatus status,
	Set<String> needs,
	LocalDate startDate,
	List<ArtistSummaryResponse> artists
) {
}
