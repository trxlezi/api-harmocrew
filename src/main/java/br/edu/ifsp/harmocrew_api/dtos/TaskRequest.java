package br.edu.ifsp.harmocrew_api.dtos;

import java.time.LocalDate;

import br.edu.ifsp.harmocrew_api.enums.TaskPriority;
import br.edu.ifsp.harmocrew_api.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TaskRequest(
	@NotBlank
	@Size(max = 160)
	String title,

	@Size(max = 3000)
	String description,

	@NotNull
	TaskStatus status,

	@NotNull
	TaskPriority priority,

	@NotNull
	LocalDate dueDate,

	@NotBlank
	@Size(max = 120)
	String responsibleName
) {
}
