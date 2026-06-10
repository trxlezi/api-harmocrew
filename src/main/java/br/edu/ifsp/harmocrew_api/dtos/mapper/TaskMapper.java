package br.edu.ifsp.harmocrew_api.dtos.mapper;

import br.edu.ifsp.harmocrew_api.dtos.TaskResponse;
import br.edu.ifsp.harmocrew_api.entities.MusicalProject;
import br.edu.ifsp.harmocrew_api.entities.Task;

public final class TaskMapper {

	private TaskMapper() {
	}

	public static TaskResponse toResponse(Task task) {
		if (task == null) {
			return null;
		}

		MusicalProject project = task.getProject();
		Long projectId = project == null ? null : project.getId();
		String projectTitle = project == null ? null : project.getTitle();

		return new TaskResponse(
			task.getId(),
			task.getTitle(),
			task.getDescription(),
			task.getStatus(),
			task.getPriority(),
			task.getDueDate(),
			task.getResponsibleName(),
			projectId,
			projectTitle);
	}
}
