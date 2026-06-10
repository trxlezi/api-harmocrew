package br.edu.ifsp.harmocrew_api.dtos;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import br.edu.ifsp.harmocrew_api.enums.RehearsalStatus;

public record RehearsalResponse(
	Long id,
	Long projectId,
	String projectTitle,
	String title,
	LocalDate date,
	LocalTime time,
	String location,
	Set<Long> participantArtistIds,
	String notes,
	RehearsalStatus status
) {
}
