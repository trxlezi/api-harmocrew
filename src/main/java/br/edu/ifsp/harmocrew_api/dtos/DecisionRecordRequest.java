package br.edu.ifsp.harmocrew_api.dtos;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DecisionRecordRequest(
	@NotBlank
	@Size(max = 160)
	String title,

	@NotBlank
	@Size(max = 3000)
	String description,

	@Size(max = 2000)
	String impact,

	LocalDate decidedAt,

	@NotNull
	Long decidedByArtistId
) {
}
