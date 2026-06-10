package br.edu.ifsp.harmocrew_api.dtos;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import br.edu.ifsp.harmocrew_api.enums.RehearsalStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RehearsalRequest(
	@NotBlank
	@Size(max = 160)
	String title,

	@NotNull
	LocalDate date,

	@NotNull
	LocalTime time,

	@NotBlank
	@Size(max = 160)
	String location,

	Set<@NotNull Long> participantArtistIds,

	@Size(max = 2000)
	String notes,

	@NotNull
	RehearsalStatus status
) {
}
