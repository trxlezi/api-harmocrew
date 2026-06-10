package br.edu.ifsp.harmocrew_api.dtos;

import java.time.LocalDate;

import br.edu.ifsp.harmocrew_api.enums.WeeklyGoalStatus;

public record WeeklyGoalResponse(
	Long id,
	Long projectId,
	String projectTitle,
	String title,
	String description,
	Long ownerArtistId,
	String ownerArtistName,
	String weekLabel,
	LocalDate dueDate,
	WeeklyGoalStatus status
) {
}
