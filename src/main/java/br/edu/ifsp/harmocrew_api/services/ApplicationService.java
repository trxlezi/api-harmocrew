package br.edu.ifsp.harmocrew_api.services;

import java.time.LocalDateTime;
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
import br.edu.ifsp.harmocrew_api.enums.ApplicationStatus;
import br.edu.ifsp.harmocrew_api.enums.NotificationType;
import br.edu.ifsp.harmocrew_api.exceptions.ResourceNotFoundException;
import br.edu.ifsp.harmocrew_api.repositories.ApplicationRepository;

@Service
public class ApplicationService {

	private final ApplicationRepository applicationRepository;
	private final MusicalProjectService projectService;
	private final ArtistService artistService;
	private final MatchingService matchingService;
	private final NotificationService notificationService;
	private final AuditLogService auditLogService;

	public ApplicationService(
		ApplicationRepository applicationRepository,
		MusicalProjectService projectService,
		ArtistService artistService,
		MatchingService matchingService,
		NotificationService notificationService,
		AuditLogService auditLogService
	) {
		this.matchingService = matchingService;
		this.notificationService = notificationService;
		this.auditLogService = auditLogService;
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
		application.setMatchScore(matchingService.score(artist, project).score());
		application.setStatusUpdatedAt(LocalDateTime.now());
		Application saved = applicationRepository.save(application);
		auditLogService.record(project.getId(), "APPLICATION_CREATED", "Application", saved.getId(), artist,
			"Candidatura de " + artist.getStageName() + " (compatibilidade " + saved.getMatchScore() + "%)");
		return ApplicationMapper.toResponse(saved);
	}

	@Transactional
	public ApplicationResponse updateStatus(Long id, ApplicationStatusUpdateRequest request) {
		Application application = findEntity(id);
		ApplicationStatus previous = application.getStatus();
		application.setStatus(request.status());
		application.setStatusUpdatedAt(LocalDateTime.now());
		if (request.evaluationNote() != null) {
			application.setEvaluationNote(request.evaluationNote().isBlank() ? null : request.evaluationNote().trim());
		}
		if (request.status() == ApplicationStatus.APPROVED) {
			application.getProject().getArtists().add(application.getArtist());
		}
		if (previous != request.status()) {
			// Retorno ao musico: ele e avisado a cada mudanca de status da candidatura.
			notificationService.notify(application.getArtist().getId(), application.getProject().getId(),
				NotificationType.APPLICATION_STATUS_CHANGED,
				"Candidatura: " + statusLabel(request.status()),
				"Sua candidatura para " + application.getProject().getTitle() + " agora esta: " + statusLabel(request.status())
					+ (application.getEvaluationNote() == null ? "" : ". Comentario: " + application.getEvaluationNote()),
				application.getId());
			auditLogService.record(application.getProject().getId(), "APPLICATION_STATUS_CHANGED", "Application",
				application.getId(), null, previous + " -> " + request.status());
		}
		return ApplicationMapper.toResponse(application);
	}

	@Transactional
	public void delete(Long id) {
		applicationRepository.delete(findEntity(id));
	}

	static String statusLabel(ApplicationStatus status) {
		return switch (status) {
			case PENDING -> "Recebida";
			case UNDER_REVIEW -> "Em avaliacao";
			case SHORTLISTED -> "Pre-selecionada";
			case APPROVED -> "Aprovada";
			case REJECTED -> "Nao selecionada";
		};
	}

	Application findEntity(Long id) {
		return applicationRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Candidatura nao encontrada: " + id));
	}
}
