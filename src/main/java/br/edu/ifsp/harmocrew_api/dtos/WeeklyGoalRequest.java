package br.edu.ifsp.harmocrew_api.dtos;

import java.time.LocalDate;

import br.edu.ifsp.harmocrew_api.enums.WeeklyGoalStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WeeklyGoalRequest(
	@NotBlank
	@Size(max = 160)
	String title,

	@Size(max = 2000)
	String description,

	@NotNull
	Long ownerArtistId,

	@NotBlank
	@Size(max = 80)
	String weekLabel,

	@NotNull
	LocalDate dueDate,

	@NotNull
	WeeklyGoalStatus status
) {
}
