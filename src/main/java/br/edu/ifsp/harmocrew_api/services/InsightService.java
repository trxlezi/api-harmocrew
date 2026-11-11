package br.edu.ifsp.harmocrew_api.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifsp.harmocrew_api.dtos.FileDtos.ProjectFileResponse;
import br.edu.ifsp.harmocrew_api.dtos.InsightDtos.DashboardResponse;
import br.edu.ifsp.harmocrew_api.dtos.InsightDtos.MemberMetrics;
import br.edu.ifsp.harmocrew_api.dtos.InsightDtos.ProjectMetrics;
import br.edu.ifsp.harmocrew_api.dtos.InsightDtos.TimelineResponse;
import br.edu.ifsp.harmocrew_api.dtos.InsightDtos.TimelineStage;
import br.edu.ifsp.harmocrew_api.dtos.InsightDtos.WeeklyPoint;
import br.edu.ifsp.harmocrew_api.dtos.TaskResponse;
import br.edu.ifsp.harmocrew_api.dtos.mapper.TaskMapper;
import br.edu.ifsp.harmocrew_api.entities.Artist;
import br.edu.ifsp.harmocrew_api.entities.MusicalProject;
import br.edu.ifsp.harmocrew_api.entities.ProjectFile;
import br.edu.ifsp.harmocrew_api.entities.Rehearsal;
import br.edu.ifsp.harmocrew_api.entities.Task;
import br.edu.ifsp.harmocrew_api.entities.WeeklyGoal;
import br.edu.ifsp.harmocrew_api.enums.ApplicationStatus;
import br.edu.ifsp.harmocrew_api.enums.DecisionStatus;
import br.edu.ifsp.harmocrew_api.enums.PollStatus;
import br.edu.ifsp.harmocrew_api.enums.ProductionStage;
import br.edu.ifsp.harmocrew_api.enums.RehearsalStatus;
import br.edu.ifsp.harmocrew_api.enums.TaskStatus;
import br.edu.ifsp.harmocrew_api.enums.WeeklyGoalStatus;
import br.edu.ifsp.harmocrew_api.repositories.MusicalProjectRepository;

/*
 * Timeline do workflow musical (Funcionalidade 3) e dashboard de performance
 * do coletivo (Funcionalidade 4). Tudo e calculado a partir dos dados ja
 * existentes: tarefas, ensaios (presenca), metas, enquetes, decisoes e arquivos.
 */
@Service
public class InsightService {

	private static final int THROUGHPUT_WEEKS = 8;

	private final MusicalProjectRepository projectRepository;
	private final MusicalProjectService projectService;

	public InsightService(MusicalProjectRepository projectRepository, MusicalProjectService projectService) {
		this.projectRepository = projectRepository;
		this.projectService = projectService;
	}

	// ---------------------------------------------------------------- timeline

	@Transactional(readOnly = true)
	public TimelineResponse timeline(Long projectId) {
		MusicalProject project = projectService.findEntity(projectId);
		Map<ProductionStage, List<Task>> byStage = project.getTasks().stream()
			.filter(task -> task.getStage() != null)
			.collect(Collectors.groupingBy(Task::getStage));

		List<TimelineStage> stages = new ArrayList<>();
		ProductionStage current = null;
		for (ProductionStage stage : ProductionStage.values()) {
			List<Task> tasks = byStage.getOrDefault(stage, List.of()).stream()
				.sorted(Comparator.comparing(InsightService::barStart, Comparator.nullsLast(Comparator.naturalOrder())))
				.toList();
			int done = (int) tasks.stream().filter(task -> task.getStatus() == TaskStatus.DONE).count();
			int overdue = (int) tasks.stream().filter(Task::isOverdue).count();
			// Etapa atual: a primeira (na ordem do workflow) que ainda tem pendencias.
			if (current == null && done < tasks.size()) {
				current = stage;
			}
			stages.add(new TimelineStage(
				stage,
				stageLabel(stage),
				stage.ordinal() + 1,
				tasks.stream().map(InsightService::barStart).filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(null),
				tasks.stream().map(Task::getDueDate).filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(null),
				tasks.size(),
				done,
				percent(done, tasks.size()),
				overdue,
				tasks.stream().map(TaskMapper::toResponse).toList()));
		}
		List<TaskResponse> unstaged = project.getTasks().stream()
			.filter(task -> task.getStage() == null)
			.sorted(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())))
			.map(TaskMapper::toResponse)
			.toList();
		LocalDate start = stages.stream().map(TimelineStage::start).filter(Objects::nonNull).min(Comparator.naturalOrder())
			.orElse(project.getStartDate());
		LocalDate end = stages.stream().map(TimelineStage::end).filter(Objects::nonNull).max(Comparator.naturalOrder())
			.orElse(null);
		return new TimelineResponse(project.getId(), project.getTitle(), start, end, current, stages, unstaged);
	}

	// --------------------------------------------------------------- dashboard

	@Transactional(readOnly = true)
	public DashboardResponse projectDashboard(Long projectId) {
		MusicalProject project = projectService.findEntity(projectId);
		return build(List.of(project), project.getId(), "PROJECT");
	}

	// Visao do coletivo: todos os projetos ou apenas os de um artista.
	@Transactional(readOnly = true)
	public DashboardResponse collectiveDashboard(Long artistId) {
		List<MusicalProject> projects = projectRepository.findAll().stream()
			.filter(project -> artistId == null
				|| project.getArtists().stream().anyMatch(artist -> artist.getId().equals(artistId)))
			.toList();
		return build(projects, null, artistId == null ? "COLLECTIVE" : "ARTIST");
	}

	private DashboardResponse build(List<MusicalProject> projects, Long projectId, String scope) {
		List<Task> tasks = projects.stream().flatMap(project -> project.getTasks().stream()).toList();
		List<Rehearsal> rehearsals = projects.stream().flatMap(project -> project.getRehearsals().stream()).toList();
		List<WeeklyGoal> goals = projects.stream().flatMap(project -> project.getWeeklyGoals().stream()).toList();

		int done = count(tasks, task -> task.getStatus() == TaskStatus.DONE);
		int inProgress = count(tasks, task -> task.getStatus() == TaskStatus.IN_PROGRESS);
		int todo = count(tasks, task -> task.getStatus() == TaskStatus.TODO);
		int overdue = count(tasks, Task::isOverdue);

		// Pontualidade: entre as tarefas concluidas com prazo, quantas foram entregues ate o prazo.
		List<Task> doneWithDue = tasks.stream()
			.filter(task -> task.getStatus() == TaskStatus.DONE && task.getDueDate() != null && task.getCompletedAt() != null)
			.toList();
		Integer onTime = doneWithDue.isEmpty() ? null
			: percent(count(doneWithDue, task -> !task.getCompletedAt().toLocalDate().isAfter(task.getDueDate())), doneWithDue.size());
		Double averageDays = tasks.stream()
			.filter(task -> task.getCompletedAt() != null && task.getStartDate() != null)
			.mapToLong(task -> Math.max(0, ChronoUnit.DAYS.between(task.getStartDate(), task.getCompletedAt().toLocalDate())))
			.average()
			.stream().boxed().map(value -> Math.round(value * 10) / 10.0).findFirst().orElse(null);

		int goalsDone = count(goals, goal -> goal.getStatus() == WeeklyGoalStatus.DONE);

		// Assiduidade: presencas / convites nos ensaios ja realizados.
		List<Rehearsal> completed = rehearsals.stream().filter(r -> r.getStatus() == RehearsalStatus.COMPLETED).toList();
		int invited = completed.stream().mapToInt(r -> invitedOf(r).size()).sum();
		int attended = completed.stream().mapToInt(r -> r.getAttendedArtistIds().size()).sum();

		Map<String, Integer> byStatus = new LinkedHashMap<>();
		for (TaskStatus status : TaskStatus.values()) {
			byStatus.put(status.name(), count(tasks, task -> task.getStatus() == status));
		}
		Map<String, Integer> byStage = new LinkedHashMap<>();
		for (ProductionStage stage : ProductionStage.values()) {
			byStage.put(stage.name(), count(tasks, task -> task.getStage() == stage));
		}
		byStage.put("NONE", count(tasks, task -> task.getStage() == null));

		List<ProjectFileResponse> importantFiles = projects.stream()
			.flatMap(project -> project.getFiles().stream())
			// Arquivos decididos primeiro, depois os atualizados mais recentemente.
			.sorted(Comparator.comparing(ProjectFile::isLocked).reversed()
				.thenComparing(InsightService::lastActivity, Comparator.reverseOrder()))
			.limit(6)
			.map(ProjectFileService::toResponse)
			.toList();

		return new DashboardResponse(
			projectId,
			scope,
			tasks.size(),
			done,
			inProgress,
			todo,
			overdue,
			percent(done, tasks.size()),
			onTime,
			averageDays,
			goals.size(),
			goalsDone,
			goals.isEmpty() ? null : percent(goalsDone, goals.size()),
			completed.size(),
			invited == 0 ? null : percent(attended, invited),
			(int) projects.stream().flatMap(project -> project.getPolls().stream())
				.filter(poll -> poll.getStatus() == PollStatus.OPEN).count(),
			(int) projects.stream().flatMap(project -> project.getDecisions().stream())
				.filter(decision -> decision.getStatus() != DecisionStatus.CANCELED).count(),
			(int) projects.stream().flatMap(project -> project.getApplications().stream())
				.filter(app -> app.getStatus() == ApplicationStatus.PENDING || app.getStatus() == ApplicationStatus.UNDER_REVIEW
					|| app.getStatus() == ApplicationStatus.SHORTLISTED).count(),
			byStatus,
			byStage,
			throughput(tasks),
			memberMetrics(projects, tasks, completed),
			projects.stream().map(this::projectMetrics).toList(),
			tasks.stream()
				.filter(task -> task.getUpdatedAt() != null)
				.sorted(Comparator.comparing(Task::getUpdatedAt).reversed())
				.limit(5)
				.map(TaskMapper::toResponse)
				.toList(),
			tasks.stream()
				.filter(task -> task.getStatus() != TaskStatus.DONE && task.getDueDate() != null)
				.sorted(Comparator.comparing(Task::getDueDate))
				.limit(5)
				.map(TaskMapper::toResponse)
				.toList(),
			importantFiles);
	}

	private ProjectMetrics projectMetrics(MusicalProject project) {
		List<Task> tasks = List.copyOf(project.getTasks());
		int done = count(tasks, task -> task.getStatus() == TaskStatus.DONE);
		List<Task> doneWithDue = tasks.stream()
			.filter(task -> task.getStatus() == TaskStatus.DONE && task.getDueDate() != null && task.getCompletedAt() != null)
			.toList();
		List<Rehearsal> completed = project.getRehearsals().stream()
			.filter(r -> r.getStatus() == RehearsalStatus.COMPLETED).toList();
		int invited = completed.stream().mapToInt(r -> invitedOf(r).size()).sum();
		int attended = completed.stream().mapToInt(r -> r.getAttendedArtistIds().size()).sum();
		ProductionStage current = Arrays.stream(ProductionStage.values())
			.filter(stage -> tasks.stream().anyMatch(task -> task.getStage() == stage && task.getStatus() != TaskStatus.DONE))
			.findFirst().orElse(null);
		return new ProjectMetrics(
			project.getId(),
			project.getTitle(),
			tasks.size(),
			done,
			count(tasks, Task::isOverdue),
			percent(done, tasks.size()),
			doneWithDue.isEmpty() ? null
				: percent(count(doneWithDue, task -> !task.getCompletedAt().toLocalDate().isAfter(task.getDueDate())), doneWithDue.size()),
			invited == 0 ? null : percent(attended, invited),
			current);
	}

	private List<MemberMetrics> memberMetrics(List<MusicalProject> projects, List<Task> tasks, List<Rehearsal> completed) {
		Map<Long, Artist> members = new LinkedHashMap<>();
		projects.forEach(project -> project.getArtists().forEach(artist -> members.putIfAbsent(artist.getId(), artist)));
		tasks.stream().map(Task::getAssignee).filter(Objects::nonNull).forEach(a -> members.putIfAbsent(a.getId(), a));

		return members.values().stream()
			.map(artist -> {
				List<Task> mine = tasks.stream().filter(task -> isResponsible(task, artist)).toList();
				int invitedCount = (int) completed.stream().filter(r -> invitedOf(r).contains(artist.getId())).count();
				int attendedCount = (int) completed.stream().filter(r -> r.getAttendedArtistIds().contains(artist.getId())).count();
				return new MemberMetrics(
					artist.getId(),
					artist.getStageName(),
					mine.size(),
					count(mine, task -> task.getStatus() == TaskStatus.DONE),
					count(mine, Task::isOverdue),
					invitedCount,
					attendedCount,
					invitedCount == 0 ? null : percent(attendedCount, invitedCount));
			})
			.sorted(Comparator.comparing(MemberMetrics::doneTasks).reversed().thenComparing(MemberMetrics::name))
			.toList();
	}

	// Tarefas criadas x concluidas por semana (ultimas 8 semanas).
	private List<WeeklyPoint> throughput(List<Task> tasks) {
		LocalDate thisWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		List<WeeklyPoint> points = new ArrayList<>();
		for (int i = THROUGHPUT_WEEKS - 1; i >= 0; i--) {
			LocalDate weekStart = thisWeek.minusWeeks(i);
			LocalDate weekEnd = weekStart.plusDays(7);
			int completedInWeek = count(tasks, task -> task.getCompletedAt() != null
				&& inRange(task.getCompletedAt().toLocalDate(), weekStart, weekEnd));
			int createdInWeek = count(tasks, task -> barStart(task) != null && inRange(barStart(task), weekStart, weekEnd));
			points.add(new WeeklyPoint(weekStart, createdInWeek, completedInWeek));
		}
		return points;
	}

	private static Set<Long> invitedOf(Rehearsal rehearsal) {
		if (!rehearsal.getParticipantArtistIds().isEmpty()) {
			return rehearsal.getParticipantArtistIds();
		}
		return rehearsal.getProject().getArtists().stream().map(Artist::getId).collect(Collectors.toSet());
	}

	private static boolean isResponsible(Task task, Artist artist) {
		if (task.getAssignee() != null) {
			return task.getAssignee().getId().equals(artist.getId());
		}
		return task.getResponsibleName() != null && task.getResponsibleName().equalsIgnoreCase(artist.getStageName());
	}

	private static LocalDate barStart(Task task) {
		return task.getStartDate() != null ? task.getStartDate() : task.getDueDate();
	}

	private static java.time.LocalDateTime lastActivity(ProjectFile file) {
		return file.latestVersion() == null ? file.getCreatedAt() : file.latestVersion().getUploadedAt();
	}

	private static boolean inRange(LocalDate date, LocalDate start, LocalDate endExclusive) {
		return !date.isBefore(start) && date.isBefore(endExclusive);
	}

	private static <T> int count(List<T> items, java.util.function.Predicate<T> predicate) {
		return (int) items.stream().filter(predicate).count();
	}

	private static int percent(int part, int total) {
		return total == 0 ? 0 : (int) Math.round(part * 100.0 / total);
	}

	static String stageLabel(ProductionStage stage) {
		return switch (stage) {
			case COMPOSITION -> "Composicao";
			case ARRANGEMENT -> "Arranjo";
			case RECORDING -> "Gravacao";
			case MIXING -> "Mixagem";
			case RELEASE -> "Lancamento";
		};
	}
}
