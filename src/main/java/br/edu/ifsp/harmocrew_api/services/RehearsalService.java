package br.edu.ifsp.harmocrew_api.services;

import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifsp.harmocrew_api.dtos.RehearsalRequest;
import br.edu.ifsp.harmocrew_api.dtos.RehearsalResponse;
import br.edu.ifsp.harmocrew_api.dtos.RehearsalStatusUpdateRequest;
import br.edu.ifsp.harmocrew_api.dtos.mapper.RehearsalMapper;
import br.edu.ifsp.harmocrew_api.entities.MusicalProject;
import br.edu.ifsp.harmocrew_api.entities.Rehearsal;
import br.edu.ifsp.harmocrew_api.exceptions.ResourceNotFoundException;
import br.edu.ifsp.harmocrew_api.repositories.RehearsalRepository;

@Service
public class RehearsalService {

	private final RehearsalRepository rehearsalRepository;
	private final MusicalProjectService projectService;

	public RehearsalService(RehearsalRepository rehearsalRepository, MusicalProjectService projectService) {
		this.rehearsalRepository = rehearsalRepository;
		this.projectService = projectService;
	}

	@Transactional(readOnly = true)
	public List<RehearsalResponse> list(Long projectId) {
		List<Rehearsal> rehearsals = projectId == null
			? rehearsalRepository.findAll()
			: rehearsalRepository.findByProjectIdOrderByDateAscTimeAsc(projectId);
		return rehearsals.stream().map(RehearsalMapper::toResponse).toList();
	}

	@Transactional
	public RehearsalResponse create(Long projectId, RehearsalRequest request) {
		MusicalProject project = projectService.findEntity(projectId);
		Rehearsal rehearsal = new Rehearsal();
		rehearsal.setProject(project);
		apply(rehearsal, request);
		return RehearsalMapper.toResponse(rehearsalRepository.save(rehearsal));
	}

	@Transactional
	public RehearsalResponse update(Long id, RehearsalRequest request) {
		Rehearsal rehearsal = findEntity(id);
		apply(rehearsal, request);
		return RehearsalMapper.toResponse(rehearsal);
	}

	@Transactional
	public RehearsalResponse updateStatus(Long id, RehearsalStatusUpdateRequest request) {
		Rehearsal rehearsal = findEntity(id);
		rehearsal.setStatus(request.status());
		return RehearsalMapper.toResponse(rehearsal);
	}

	@Transactional
	public void delete(Long id) {
		rehearsalRepository.delete(findEntity(id));
	}

	private Rehearsal findEntity(Long id) {
		return rehearsalRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Ensaio nao encontrado: " + id));
	}

	private void apply(Rehearsal rehearsal, RehearsalRequest request) {
		rehearsal.setTitle(request.title().trim());
		rehearsal.setDate(request.date());
		rehearsal.setTime(request.time());
		rehearsal.setLocation(request.location().trim());
		rehearsal.setNotes(request.notes());
		rehearsal.setStatus(request.status());
		rehearsal.setParticipantArtistIds(request.participantArtistIds() == null
			? new LinkedHashSet<>()
			: new LinkedHashSet<>(request.participantArtistIds()));
	}
}
