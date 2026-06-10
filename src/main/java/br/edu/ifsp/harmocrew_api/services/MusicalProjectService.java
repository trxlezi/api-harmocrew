package br.edu.ifsp.harmocrew_api.services;

import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifsp.harmocrew_api.dtos.MusicalProjectRequest;
import br.edu.ifsp.harmocrew_api.dtos.MusicalProjectResponse;
import br.edu.ifsp.harmocrew_api.dtos.MusicalProjectSummaryResponse;
import br.edu.ifsp.harmocrew_api.dtos.mapper.MusicalProjectMapper;
import br.edu.ifsp.harmocrew_api.entities.Artist;
import br.edu.ifsp.harmocrew_api.entities.MusicalProject;
import br.edu.ifsp.harmocrew_api.exceptions.ResourceNotFoundException;
import br.edu.ifsp.harmocrew_api.repositories.MusicalProjectRepository;

@Service
public class MusicalProjectService {

	private final MusicalProjectRepository projectRepository;
	private final ArtistService artistService;

	public MusicalProjectService(MusicalProjectRepository projectRepository, ArtistService artistService) {
		this.projectRepository = projectRepository;
		this.artistService = artistService;
	}

	@Transactional(readOnly = true)
	public List<MusicalProjectResponse> list() {
		return projectRepository.findAll().stream().map(MusicalProjectMapper::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<MusicalProjectSummaryResponse> listSummaries() {
		return projectRepository.findAll().stream().map(MusicalProjectMapper::toSummary).toList();
	}

	@Transactional(readOnly = true)
	public MusicalProjectResponse get(Long id) {
		return MusicalProjectMapper.toResponse(findEntity(id));
	}

	@Transactional
	public MusicalProjectResponse create(MusicalProjectRequest request) {
		MusicalProject project = new MusicalProject();
		apply(project, request);
		return MusicalProjectMapper.toResponse(projectRepository.save(project));
	}

	@Transactional
	public MusicalProjectResponse update(Long id, MusicalProjectRequest request) {
		MusicalProject project = findEntity(id);
		apply(project, request);
		return MusicalProjectMapper.toResponse(project);
	}

	@Transactional
	public MusicalProjectResponse addArtist(Long projectId, Long artistId) {
		MusicalProject project = findEntity(projectId);
		Artist artist = artistService.findEntity(artistId);
		project.getArtists().add(artist);
		return MusicalProjectMapper.toResponse(project);
	}

	@Transactional
	public void delete(Long id) {
		projectRepository.delete(findEntity(id));
	}

	MusicalProject findEntity(Long id) {
		return projectRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Projeto nao encontrado: " + id));
	}

	private void apply(MusicalProject project, MusicalProjectRequest request) {
		project.setTitle(request.title().trim());
		project.setDescription(request.description());
		project.setMusicalStyle(request.musicalStyle().trim());
		project.setStatus(request.status());
		project.setNeeds(request.needs() == null ? new LinkedHashSet<>() : new LinkedHashSet<>(request.needs()));
		project.setStartDate(request.startDate());
	}
}
