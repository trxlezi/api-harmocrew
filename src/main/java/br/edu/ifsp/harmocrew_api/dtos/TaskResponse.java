package br.edu.ifsp.harmocrew_api.dtos;

import java.time.LocalDate;

import br.edu.ifsp.harmocrew_api.enums.TaskPriority;
import br.edu.ifsp.harmocrew_api.enums.TaskStatus;

public record TaskResponse(
	Long id,
	String title,
	String description,
	TaskStatus status,
	TaskPriority priority,
	LocalDate dueDate,
	String responsibleName,
	Long projectId,
	String projectTitle
) {
}
