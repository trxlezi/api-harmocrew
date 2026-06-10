package br.edu.ifsp.harmocrew_api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApplicationRequest(
	@NotNull
	Long artistId,

	@NotBlank
	@Size(max = 2000)
	String message,

	@NotBlank
	@Size(max = 120)
	String specialty,

	@NotBlank
	@Size(max = 160)
	String availability
) {
}
