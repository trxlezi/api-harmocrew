package br.edu.ifsp.harmocrew_api.dtos;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.edu.ifsp.harmocrew_api.dtos.FileDtos.ProjectFileResponse;
import br.edu.ifsp.harmocrew_api.enums.ProductionStage;
import jakarta.validation.constraints.NotNull;

// DTOs da timeline (Funcionalidade 3) e do dashboard de metricas (Funcionalidade 4).
public final class InsightDtos {

	private InsightDtos() {
	}

	// ---------- Timeline / Gantt
	public record TimelineStage(
		ProductionStage stage,
		String label,
		int order,
		LocalDate start,
		LocalDate end,
		int totalTasks,
		int doneTasks,
		int progressPercent,
		int overdueTasks,
		List<TaskResponse> tasks
	) {
	}

	public record TimelineResponse(
		Long projectId,
		String projectTitle,
		LocalDate start,
		LocalDate end,
		ProductionStage currentStage,
		List<TimelineStage> stages,
		List<TaskResponse> unstagedTasks
	) {
	}

	// ---------- Dashboard
	public record AttendanceRequest(
		@NotNull
		Set<Long> attendedArtistIds
	) {
	}

	public record MemberMetrics(
		Long artistId,
		String name,
		int assignedTasks,
		int doneTasks,
		int overdueTasks,
		int rehearsalsInvited,
		int rehearsalsAttended,
		Integer attendancePercent
	) {
	}

	public record WeeklyPoint(LocalDate weekStart, int created, int completed) {
	}

	public record ProjectMetrics(
		Long projectId,
		String title,
		int totalTasks,
		int doneTasks,
		int overdueTasks,
		int completionPercent,
		Integer onTimePercent,
		Integer attendancePercent,
		ProductionStage currentStage
	) {
	}

	public record DashboardResponse(
		Long projectId,
		String scope,
		int totalTasks,
		int doneTasks,
		int inProgressTasks,
		int todoTasks,
		int overdueTasks,
		int completionPercent,
		Integer onTimePercent,
		Double averageCompletionDays,
		int weeklyGoalsTotal,
		int weeklyGoalsDone,
		Integer weeklyGoalsPercent,
		int rehearsalsCompleted,
		Integer attendancePercent,
		int openPolls,
		int decisionsRegistered,
		int pendingApplications,
		Map<String, Integer> tasksByStatus,
		Map<String, Integer> tasksByStage,
		List<WeeklyPoint> weeklyThroughput,
		List<MemberMetrics> members,
		List<ProjectMetrics> projects,
		List<TaskResponse> recentTasks,
		List<TaskResponse> upcomingDeadlines,
		List<ProjectFileResponse> importantFiles
	) {
	}
}
