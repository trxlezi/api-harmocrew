package br.edu.ifsp.harmocrew_api.dtos;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArtistRequest(
	@NotBlank
	@Size(max = 120)
	String stageName,

	@Size(max = 2000)
	String bio,

	@NotBlank
	@Size(max = 120)
	String mainSpecialty,

	Set<@NotBlank @Size(max = 80) String> instruments,

	Set<@NotBlank @Size(max = 80) String> musicalStyles,

	@NotBlank
	@Size(max = 160)
	String availability,

	@Size(max = 120)
	String city
) {
}
