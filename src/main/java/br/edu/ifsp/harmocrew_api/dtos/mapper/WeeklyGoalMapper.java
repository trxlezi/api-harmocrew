package br.edu.ifsp.harmocrew_api.dtos.mapper;

import br.edu.ifsp.harmocrew_api.dtos.WeeklyGoalResponse;
import br.edu.ifsp.harmocrew_api.entities.Artist;
import br.edu.ifsp.harmocrew_api.entities.MusicalProject;
import br.edu.ifsp.harmocrew_api.entities.WeeklyGoal;

public final class WeeklyGoalMapper {

	private WeeklyGoalMapper() {
	}

	public static WeeklyGoalResponse toResponse(WeeklyGoal goal) {
		Artist owner = goal.getOwnerArtist();
		MusicalProject project = goal.getProject();
		return new WeeklyGoalResponse(
			goal.getId(),
			project == null ? null : project.getId(),
			project == null ? null : project.getTitle(),
			goal.getTitle(),
			goal.getDescription(),
			owner == null ? null : owner.getId(),
			owner == null ? null : owner.getStageName(),
			goal.getWeekLabel(),
			goal.getDueDate(),
			goal.getStatus());
	}
}
