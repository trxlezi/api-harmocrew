package br.edu.ifsp.harmocrew_api.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifsp.harmocrew_api.dtos.TaskRequest;
import br.edu.ifsp.harmocrew_api.dtos.TaskResponse;
import br.edu.ifsp.harmocrew_api.dtos.TaskStatusUpdateRequest;
import br.edu.ifsp.harmocrew_api.dtos.mapper.TaskMapper;
import br.edu.ifsp.harmocrew_api.entities.MusicalProject;
import br.edu.ifsp.harmocrew_api.entities.Task;
import br.edu.ifsp.harmocrew_api.enums.NotificationType;
import br.edu.ifsp.harmocrew_api.enums.TaskStatus;
import br.edu.ifsp.harmocrew_api.exceptions.BusinessException;
import br.edu.ifsp.harmocrew_api.exceptions.ResourceNotFoundException;
import br.edu.ifsp.harmocrew_api.repositories.TaskRepository;

@Service
public class TaskService {

	private final TaskRepository taskRepository;
	private final MusicalProjectService projectService;
	private final ArtistService artistService;
	private final NotificationService notificationService;
	private final AuditLogService auditLogService;

	public TaskService(TaskRepository taskRepository, MusicalProjectService projectService, ArtistService artistService,
		NotificationService notificationService, AuditLogService auditLogService) {
		this.taskRepository = taskRepository;
		this.projectService = projectService;
		this.artistService = artistService;
		this.notificationService = notificationService;
		this.auditLogService = auditLogService;
	}

	@Transactional(readOnly = true)
	public List<TaskResponse> list(Long projectId) {
		List<Task> tasks = projectId == null ? taskRepository.findAll() : taskRepository.findByProjectId(projectId);
		return tasks.stream().map(TaskMapper::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public TaskResponse get(Long id) {
		return TaskMapper.toResponse(findEntity(id));
	}

	@Transactional
	public TaskResponse create(Long projectId, TaskRequest request) {
		MusicalProject project = projectService.findEntity(projectId);
		Task task = new Task();
		task.setProject(project);
		apply(task, request);
		Task saved = taskRepository.save(task);
		auditLogService.record(project.getId(), "TASK_CREATED", "Task", saved.getId(), saved.getAssignee(), saved.getTitle());
		notifyAssigned(saved);
		return TaskMapper.toResponse(saved);
	}

	@Transactional
	public TaskResponse update(Long id, TaskRequest request) {
		Task task = findEntity(id);
		TaskStatus previousStatus = task.getStatus();
		Long previousAssignee = task.getAssignee() == null ? null : task.getAssignee().getId();
		apply(task, request);
		Long newAssignee = task.getAssignee() == null ? null : task.getAssignee().getId();
		if (newAssignee != null && !newAssignee.equals(previousAssignee)) {
			notifyAssigned(task);
		}
		if (previousStatus != task.getStatus()) {
			notifyStatus(task, previousStatus);
		}
		return TaskMapper.toResponse(task);
	}

	@Transactional
	public TaskResponse updateStatus(Long id, TaskStatusUpdateRequest request) {
		Task task = findEntity(id);
		TaskStatus previousStatus = task.getStatus();
		task.setStatus(request.status());
		updateCompletion(task);
		if (previousStatus != task.getStatus()) {
			notifyStatus(task, previousStatus);
		}
		return TaskMapper.toResponse(task);
	}

	@Transactional
	public void delete(Long id) {
		taskRepository.delete(findEntity(id));
	}

	private Task findEntity(Long id) {
		return taskRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Tarefa nao encontrada: " + id));
	}

	private void apply(Task task, TaskRequest request) {
		task.setTitle(request.title().trim());
		task.setDescription(request.description());
		task.setStatus(request.status());
		task.setPriority(request.priority());
		task.setResponsibleName(request.responsibleName().trim());
		if (request.dueDate() != null && !request.dueDate().equals(task.getDueDate())) {
			task.setOverdueNotifiedAt(null);
		}
		task.setDueDate(request.dueDate());
		task.setStage(request.stage());
		task.setStartDate(request.startDate());
		if (request.startDate() != null && request.dueDate() != null && request.startDate().isAfter(request.dueDate())) {
			throw new BusinessException("A data de inicio nao pode ser depois do prazo");
		}
		task.setAssignee(request.assigneeArtistId() == null ? null : artistService.findEntity(request.assigneeArtistId()));
		updateCompletion(task);
	}

	private void updateCompletion(Task task) {
		if (task.getStatus() == TaskStatus.DONE && task.getCompletedAt() == null) {
			task.setCompletedAt(LocalDateTime.now());
		}
		else if (task.getStatus() != TaskStatus.DONE) {
			task.setCompletedAt(null);
		}
	}

	private void notifyAssigned(Task task) {
		if (task.getAssignee() == null) {
			return;
		}
		notificationService.notify(task.getAssignee().getId(), task.getProject().getId(), NotificationType.TASK_ASSIGNED,
			"Nova tarefa para voce", task.getTitle() + (task.getDueDate() == null ? "" : " - prazo " + task.getDueDate()),
			task.getId());
		auditLogService.record(task.getProject().getId(), "TASK_ASSIGNED", "Task", task.getId(), null,
			task.getTitle() + " -> " + task.getAssignee().getStageName());
	}

	private void notifyStatus(Task task, TaskStatus previous) {
		notificationService.notifyAll(task.getProject().getArtists(), null, task.getProject().getId(),
			NotificationType.TASK_STATUS_CHANGED, "Tarefa atualizada: " + task.getTitle(), previous + " -> " + task.getStatus(),
			task.getId());
		auditLogService.record(task.getProject().getId(), "TASK_STATUS_CHANGED", "Task", task.getId(), null,
			task.getTitle() + ": " + previous + " -> " + task.getStatus());
	}

	/*
	 * Verificacao periodica de prazos estourados (a cada 5 min por padrao).
	 * Alerta o responsavel - ou todo o grupo se a tarefa nao tem dono - uma unica vez.
	 */
	@Scheduled(fixedDelayString = "${app.tasks.overdue-check-ms:300000}", initialDelay = 20000)
	@Transactional
	public int alertOverdueTasks() {
		List<Task> overdue = taskRepository.findByStatusNotAndDueDateBeforeAndOverdueNotifiedAtIsNull(TaskStatus.DONE, LocalDate.now());
		for (Task task : overdue) {
			String title = "Prazo estourado: " + task.getTitle();
			String body = "Venceu em " + task.getDueDate() + " (" + task.getProject().getTitle() + ")";
			if (task.getAssignee() != null) {
				notificationService.notify(task.getAssignee().getId(), task.getProject().getId(), NotificationType.TASK_OVERDUE,
					title, body, task.getId());
			}
			else {
				notificationService.notifyAll(task.getProject().getArtists(), null, task.getProject().getId(),
					NotificationType.TASK_OVERDUE, title, body, task.getId());
			}
			task.setOverdueNotifiedAt(LocalDateTime.now());
		}
		return overdue.size();
	}
}
