package br.edu.ifsp.harmocrew_api.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifsp.harmocrew_api.dtos.DecisionRecordRequest;
import br.edu.ifsp.harmocrew_api.dtos.DecisionRecordResponse;
import br.edu.ifsp.harmocrew_api.dtos.DecisionStatusUpdateRequest;
import br.edu.ifsp.harmocrew_api.dtos.mapper.DecisionRecordMapper;
import br.edu.ifsp.harmocrew_api.entities.Artist;
import br.edu.ifsp.harmocrew_api.entities.DecisionRecord;
import br.edu.ifsp.harmocrew_api.entities.MusicalProject;
import br.edu.ifsp.harmocrew_api.exceptions.ResourceNotFoundException;
import br.edu.ifsp.harmocrew_api.repositories.DecisionRecordRepository;

@Service
public class DecisionRecordService {

	private final DecisionRecordRepository decisionRepository;
	private final MusicalProjectService projectService;
	private final ArtistService artistService;

	public DecisionRecordService(
		DecisionRecordRepository decisionRepository,
		MusicalProjectService projectService,
		ArtistService artistService
	) {
		this.decisionRepository = decisionRepository;
		this.projectService = projectService;
		this.artistService = artistService;
	}

	@Transactional(readOnly = true)
	public List<DecisionRecordResponse> list(Long projectId) {
		List<DecisionRecord> decisions = projectId == null
			? decisionRepository.findAll()
			: decisionRepository.findByProjectIdOrderByDecidedAtDesc(projectId);
		return decisions.stream().map(DecisionRecordMapper::toResponse).toList();
	}

	@Transactional
	public DecisionRecordResponse create(Long projectId, DecisionRecordRequest request) {
		MusicalProject project = projectService.findEntity(projectId);
		Artist artist = artistService.findEntity(request.decidedByArtistId());
		DecisionRecord decision = new DecisionRecord();
		decision.setProject(project);
		decision.setDecidedByArtist(artist);
		apply(decision, request);
		return DecisionRecordMapper.toResponse(decisionRepository.save(decision));
	}

	@Transactional
	public DecisionRecordResponse update(Long id, DecisionRecordRequest request) {
		DecisionRecord decision = findEntity(id);
		decision.setDecidedByArtist(artistService.findEntity(request.decidedByArtistId()));
		apply(decision, request);
		return DecisionRecordMapper.toResponse(decision);
	}

	@Transactional
	public DecisionRecordResponse updateStatus(Long id, DecisionStatusUpdateRequest request) {
		DecisionRecord decision = findEntity(id);
		decision.setStatus(request.status());
		return DecisionRecordMapper.toResponse(decision);
	}

	@Transactional
	public void delete(Long id) {
		decisionRepository.delete(findEntity(id));
	}

	private DecisionRecord findEntity(Long id) {
		return decisionRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Decisao nao encontrada: " + id));
	}

	private void apply(DecisionRecord decision, DecisionRecordRequest request) {
		decision.setTitle(request.title().trim());
		decision.setDescription(request.description().trim());
		decision.setImpact(request.impact());
		decision.setDecidedAt(request.decidedAt() == null ? LocalDate.now() : request.decidedAt());
	}
}
