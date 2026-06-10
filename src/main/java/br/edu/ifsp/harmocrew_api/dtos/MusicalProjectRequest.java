package br.edu.ifsp.harmocrew_api.dtos;

import java.time.LocalDate;
import java.util.Set;

import br.edu.ifsp.harmocrew_api.enums.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MusicalProjectRequest(
	@NotBlank
	@Size(max = 160)
	String title,

	@Size(max = 3000)
	String description,

	@NotBlank
	@Size(max = 120)
	String musicalStyle,

	@NotNull
	ProjectStatus status,

	Set<@NotBlank @Size(max = 80) String> needs,

	@NotNull
	LocalDate startDate
) {
}
