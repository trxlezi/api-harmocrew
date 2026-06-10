package br.edu.ifsp.harmocrew_api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
	@NotBlank
	@Size(max = 120)
	String name,

	@NotBlank
	@Email
	@Size(max = 160)
	String email,

	@NotBlank
	@Size(min = 6, max = 120)
	String password,

	@Size(max = 120)
	String stageName,

	@Size(max = 120)
	String mainSpecialty
) {
}
