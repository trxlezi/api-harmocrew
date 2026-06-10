package br.edu.ifsp.harmocrew_api.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifsp.harmocrew_api.dtos.TaskRequest;
import br.edu.ifsp.harmocrew_api.dtos.TaskResponse;
import br.edu.ifsp.harmocrew_api.dtos.TaskStatusUpdateRequest;
import br.edu.ifsp.harmocrew_api.dtos.mapper.TaskMapper;
import br.edu.ifsp.harmocrew_api.entities.MusicalProject;
import br.edu.ifsp.harmocrew_api.entities.Task;
import br.edu.ifsp.harmocrew_api.exceptions.ResourceNotFoundException;
import br.edu.ifsp.harmocrew_api.repositories.TaskRepository;

@Service
public class TaskService {

	private final TaskRepository taskRepository;
	private final MusicalProjectService projectService;

	public TaskService(TaskRepository taskRepository, MusicalProjectService projectService) {
		this.taskRepository = taskRepository;
		this.projectService = projectService;
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
		return TaskMapper.toResponse(taskRepository.save(task));
	}

	@Transactional
	public TaskResponse update(Long id, TaskRequest request) {
		Task task = findEntity(id);
		apply(task, request);
		return TaskMapper.toResponse(task);
	}

	@Transactional
	public TaskResponse updateStatus(Long id, TaskStatusUpdateRequest request) {
		Task task = findEntity(id);
		task.setStatus(request.status());
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
		task.setDueDate(request.dueDate());
		task.setResponsibleName(request.responsibleName().trim());
	}
}
