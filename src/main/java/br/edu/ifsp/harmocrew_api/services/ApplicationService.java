package br.edu.ifsp.harmocrew_api.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifsp.harmocrew_api.dtos.ApplicationRequest;
import br.edu.ifsp.harmocrew_api.dtos.ApplicationResponse;
import br.edu.ifsp.harmocrew_api.dtos.ApplicationStatusUpdateRequest;
import br.edu.ifsp.harmocrew_api.dtos.mapper.ApplicationMapper;
import br.edu.ifsp.harmocrew_api.entities.Application;
import br.edu.ifsp.harmocrew_api.entities.Artist;
import br.edu.ifsp.harmocrew_api.entities.MusicalProject;
import br.edu.ifsp.harmocrew_api.exceptions.ResourceNotFoundException;
import br.edu.ifsp.harmocrew_api.repositories.ApplicationRepository;

@Service
public class ApplicationService {

	private final ApplicationRepository applicationRepository;
	private final MusicalProjectService projectService;
	private final ArtistService artistService;

	public ApplicationService(
		ApplicationRepository applicationRepository,
		MusicalProjectService projectService,
		ArtistService artistService
	) {
		this.applicationRepository = applicationRepository;
		this.projectService = projectService;
		this.artistService = artistService;
	}

	@Transactional(readOnly = true)
	public List<ApplicationResponse> list(Long projectId, Long artistId) {
		List<Application> applications;
		if (projectId != null) {
			applications = applicationRepository.findByProjectId(projectId);
		}
		else if (artistId != null) {
			applications = applicationRepository.findByArtistId(artistId);
		}
		else {
			applications = applicationRepository.findAll();
		}
		return applications.stream().map(ApplicationMapper::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public ApplicationResponse get(Long id) {
		return ApplicationMapper.toResponse(findEntity(id));
	}

	@Transactional
	public ApplicationResponse create(Long projectId, ApplicationRequest request) {
		MusicalProject project = projectService.findEntity(projectId);
		Artist artist = artistService.findEntity(request.artistId());
		Application application = new Application();
		application.setProject(project);
		application.setArtist(artist);
		application.setMessage(request.message().trim());
		application.setSpecialty(request.specialty().trim());
		application.setAvailability(request.availability().trim());
		return ApplicationMapper.toResponse(applicationRepository.save(application));
	}

	@Transactional
	public ApplicationResponse updateStatus(Long id, ApplicationStatusUpdateRequest request) {
		Application application = findEntity(id);
		application.setStatus(request.status());
		return ApplicationMapper.toResponse(application);
	}

	@Transactional
	public void delete(Long id) {
		applicationRepository.delete(findEntity(id));
	}

	private Application findEntity(Long id) {
		return applicationRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Candidatura nao encontrada: " + id));
	}
}
