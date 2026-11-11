package br.edu.ifsp.harmocrew_api.dtos;

import java.time.LocalDateTime;

import br.edu.ifsp.harmocrew_api.enums.ProductionStage;

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
	String projectTitle,
	ProductionStage stage,
	LocalDate startDate,
	Long assigneeArtistId,
	String assigneeName,
	LocalDateTime completedAt,
	LocalDateTime updatedAt,
	boolean overdue
) {
}
