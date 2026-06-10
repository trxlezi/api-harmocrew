package br.edu.ifsp.harmocrew_api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
	@NotBlank
	@Email
	String email,

	@NotBlank
	String password
) {
}
