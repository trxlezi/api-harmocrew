package br.edu.ifsp.harmocrew_api.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifsp.harmocrew_api.dtos.WeeklyGoalRequest;
import br.edu.ifsp.harmocrew_api.dtos.WeeklyGoalResponse;
import br.edu.ifsp.harmocrew_api.dtos.WeeklyGoalStatusUpdateRequest;
import br.edu.ifsp.harmocrew_api.dtos.mapper.WeeklyGoalMapper;
import br.edu.ifsp.harmocrew_api.entities.Artist;
import br.edu.ifsp.harmocrew_api.entities.MusicalProject;
import br.edu.ifsp.harmocrew_api.entities.WeeklyGoal;
import br.edu.ifsp.harmocrew_api.exceptions.ResourceNotFoundException;
import br.edu.ifsp.harmocrew_api.repositories.WeeklyGoalRepository;

@Service
public class WeeklyGoalService {

	private final WeeklyGoalRepository goalRepository;
	private final MusicalProjectService projectService;
	private final ArtistService artistService;

	public WeeklyGoalService(
		WeeklyGoalRepository goalRepository,
		MusicalProjectService projectService,
		ArtistService artistService
	) {
		this.goalRepository = goalRepository;
		this.projectService = projectService;
		this.artistService = artistService;
	}

	@Transactional(readOnly = true)
	public List<WeeklyGoalResponse> list(Long projectId, Long ownerArtistId) {
		List<WeeklyGoal> goals;
		if (projectId != null) {
			goals = goalRepository.findByProjectId(projectId);
		}
		else if (ownerArtistId != null) {
			goals = goalRepository.findByOwnerArtistId(ownerArtistId);
		}
		else {
			goals = goalRepository.findAll();
		}
		return goals.stream().map(WeeklyGoalMapper::toResponse).toList();
	}

	@Transactional
	public WeeklyGoalResponse create(Long projectId, WeeklyGoalRequest request) {
		MusicalProject project = projectService.findEntity(projectId);
		Artist owner = artistService.findEntity(request.ownerArtistId());
		WeeklyGoal goal = new WeeklyGoal();
		goal.setProject(project);
		goal.setOwnerArtist(owner);
		apply(goal, request);
		return WeeklyGoalMapper.toResponse(goalRepository.save(goal));
	}

	@Transactional
	public WeeklyGoalResponse update(Long id, WeeklyGoalRequest request) {
		WeeklyGoal goal = findEntity(id);
		goal.setOwnerArtist(artistService.findEntity(request.ownerArtistId()));
		apply(goal, request);
		return WeeklyGoalMapper.toResponse(goal);
	}

	@Transactional
	public WeeklyGoalResponse updateStatus(Long id, WeeklyGoalStatusUpdateRequest request) {
		WeeklyGoal goal = findEntity(id);
		goal.setStatus(request.status());
		return WeeklyGoalMapper.toResponse(goal);
	}

	@Transactional
	public void delete(Long id) {
		goalRepository.delete(findEntity(id));
	}

	private WeeklyGoal findEntity(Long id) {
		return goalRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Meta semanal nao encontrada: " + id));
	}

	private void apply(WeeklyGoal goal, WeeklyGoalRequest request) {
		goal.setTitle(request.title().trim());
		goal.setDescription(request.description());
		goal.setWeekLabel(request.weekLabel().trim());
		goal.setDueDate(request.dueDate());
		goal.setStatus(request.status());
	}
}
