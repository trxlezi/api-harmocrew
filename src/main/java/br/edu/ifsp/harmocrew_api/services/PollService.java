package br.edu.ifsp.harmocrew_api.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifsp.harmocrew_api.dtos.PollDtos.ClosePollRequest;
import br.edu.ifsp.harmocrew_api.dtos.PollDtos.PollOptionRequest;
import br.edu.ifsp.harmocrew_api.dtos.PollDtos.PollOptionResponse;
import br.edu.ifsp.harmocrew_api.dtos.PollDtos.PollRequest;
import br.edu.ifsp.harmocrew_api.dtos.PollDtos.PollResponse;
import br.edu.ifsp.harmocrew_api.dtos.PollDtos.VoteRequest;
import br.edu.ifsp.harmocrew_api.entities.Artist;
import br.edu.ifsp.harmocrew_api.entities.DecisionRecord;
import br.edu.ifsp.harmocrew_api.entities.FileVersion;
import br.edu.ifsp.harmocrew_api.entities.MusicalProject;
import br.edu.ifsp.harmocrew_api.entities.Poll;
import br.edu.ifsp.harmocrew_api.entities.PollOption;
import br.edu.ifsp.harmocrew_api.entities.PollVote;
import br.edu.ifsp.harmocrew_api.enums.DecisionStatus;
import br.edu.ifsp.harmocrew_api.enums.NotificationType;
import br.edu.ifsp.harmocrew_api.enums.PollStatus;
import br.edu.ifsp.harmocrew_api.exceptions.BusinessException;
import br.edu.ifsp.harmocrew_api.exceptions.ResourceNotFoundException;
import br.edu.ifsp.harmocrew_api.repositories.DecisionRecordRepository;
import br.edu.ifsp.harmocrew_api.repositories.PollRepository;

/*
 * Enquetes de decisao em grupo (Funcionalidade 2).
 *
 * Fluxo: criar enquete -> integrantes votam (1 voto por artista, pode trocar
 * enquanto aberta) -> encerrar (manual ou automatico no prazo) -> o resultado
 * vira DecisionRecord + log de auditoria e, se a opcao vencedora e uma versao
 * de arquivo, o arquivo e travado nessa versao.
 */
@Service
public class PollService {

	private final PollRepository pollRepository;
	private final DecisionRecordRepository decisionRepository;
	private final MusicalProjectService projectService;
	private final ProjectFileService fileService;
	private final AuditLogService auditLogService;
	private final NotificationService notificationService;
	private final CurrentArtistResolver currentArtist;

	public PollService(
		PollRepository pollRepository,
		DecisionRecordRepository decisionRepository,
		MusicalProjectService projectService,
		ProjectFileService fileService,
		AuditLogService auditLogService,
		NotificationService notificationService,
		CurrentArtistResolver currentArtist
	) {
		this.pollRepository = pollRepository;
		this.decisionRepository = decisionRepository;
		this.projectService = projectService;
		this.fileService = fileService;
		this.auditLogService = auditLogService;
		this.notificationService = notificationService;
		this.currentArtist = currentArtist;
	}

	@Transactional(readOnly = true)
	public List<PollResponse> list(Long projectId) {
		return pollRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream().map(PollService::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public PollResponse get(Long id) {
		return toResponse(findEntity(id));
	}

	@Transactional
	public PollResponse create(Long projectId, PollRequest request) {
		MusicalProject project = projectService.findEntity(projectId);
		if (request.closesAt() != null && request.closesAt().isBefore(LocalDateTime.now())) {
			throw new BusinessException("O prazo da enquete precisa estar no futuro");
		}
		Set<String> labels = new HashSet<>();
		for (PollOptionRequest option : request.options()) {
			if (!labels.add(option.label().trim().toLowerCase())) {
				throw new BusinessException("Opcoes repetidas: " + option.label());
			}
		}
		Artist actor = currentArtist.resolve(request.createdByArtistId());
		Poll poll = new Poll();
		poll.setProject(project);
		poll.setQuestion(request.question().trim());
		poll.setDescription(request.description());
		poll.setClosesAt(request.closesAt());
		poll.setCreatedBy(actor);
		int position = 0;
		for (PollOptionRequest optionRequest : request.options()) {
			PollOption option = new PollOption();
			option.setPoll(poll);
			option.setLabel(optionRequest.label().trim());
			option.setPosition(position++);
			if (optionRequest.fileVersionId() != null) {
				FileVersion version = fileService.findVersion(optionRequest.fileVersionId());
				if (!version.getFile().getProject().getId().equals(projectId)) {
					throw new BusinessException("A versao de arquivo pertence a outro projeto");
				}
				option.setFileVersion(version);
			}
			poll.getOptions().add(option);
		}
		Poll saved = pollRepository.save(poll);
		auditLogService.record(projectId, "POLL_OPENED", "Poll", saved.getId(), actor,
			"Enquete aberta: " + saved.getQuestion() + " (" + saved.getOptions().size() + " opcoes)");
		notificationService.notifyAll(project.getArtists(), actor == null ? null : actor.getId(), projectId,
			NotificationType.POLL_OPENED, "Nova enquete", saved.getQuestion(), saved.getId());
		return toResponse(saved);
	}

	@Transactional
	public PollResponse vote(Long pollId, VoteRequest request) {
		Poll poll = findEntity(pollId);
		ensureOpen(poll);
		Artist voter = currentArtist.resolve(request.artistId());
		if (voter == null) {
			throw new BusinessException("Informe o artista que esta votando");
		}
		MusicalProject project = poll.getProject();
		if (!project.getArtists().isEmpty() && project.getArtists().stream().noneMatch(a -> a.getId().equals(voter.getId()))) {
			throw new BusinessException("Somente integrantes do projeto podem votar");
		}
		PollOption option = poll.getOptions().stream()
			.filter(candidate -> candidate.getId().equals(request.optionId()))
			.findFirst()
			.orElseThrow(() -> new BusinessException("Opcao invalida para esta enquete"));

		PollVote vote = poll.getVotes().stream()
			.filter(existing -> existing.getArtist().getId().equals(voter.getId()))
			.findFirst()
			.orElse(null);
		String action;
		if (vote == null) {
			vote = new PollVote();
			vote.setPoll(poll);
			vote.setArtist(voter);
			poll.getVotes().add(vote);
			action = "POLL_VOTE";
		}
		else {
			action = "POLL_VOTE_CHANGED";
		}
		vote.setOption(option);
		vote.setVotedAt(LocalDateTime.now());
		auditLogService.record(project.getId(), action, "Poll", poll.getId(), voter,
			voter.getStageName() + " votou em \"" + option.getLabel() + "\"");
		return toResponse(poll);
	}

	@Transactional
	public PollResponse close(Long pollId, ClosePollRequest request) {
		Poll poll = findEntity(pollId);
		if (poll.getStatus() == PollStatus.CLOSED) {
			throw new BusinessException("Enquete ja encerrada");
		}
		Artist actor = currentArtist.resolve(request == null ? null : request.artistId());
		doClose(poll, actor, request == null ? null : request.tieBreakOptionId());
		return toResponse(poll);
	}

	// Encerramento automatico das enquetes cujo prazo passou (a cada minuto).
	@Scheduled(fixedDelayString = "${app.polls.auto-close-ms:60000}", initialDelay = 30000)
	@Transactional
	public void closeExpiredPolls() {
		for (Poll poll : pollRepository.findByStatusAndClosesAtBefore(PollStatus.OPEN, LocalDateTime.now())) {
			try {
				doClose(poll, null, null);
			}
			catch (BusinessException tie) {
				// Empate ou sem votos: mantem aberta e registra para o grupo decidir.
				poll.setClosesAt(null);
				auditLogService.record(poll.getProject().getId(), "POLL_DEADLINE_REACHED", "Poll", poll.getId(), null,
					"Prazo encerrado sem vencedor: " + tie.getMessage());
			}
		}
	}

	private void doClose(Poll poll, Artist actor, Long tieBreakOptionId) {
		Map<Long, Long> counts = poll.getVotes().stream()
			.collect(Collectors.groupingBy(vote -> vote.getOption().getId(), Collectors.counting()));
		if (counts.isEmpty()) {
			throw new BusinessException("Nenhum voto registrado");
		}
		long max = counts.values().stream().max(Long::compare).orElse(0L);
		List<PollOption> leaders = poll.getOptions().stream()
			.filter(option -> counts.getOrDefault(option.getId(), 0L) == max)
			.toList();
		PollOption winner;
		boolean tie = leaders.size() > 1;
		if (tie) {
			if (tieBreakOptionId == null) {
				throw new BusinessException("Empate entre: "
					+ leaders.stream().map(PollOption::getLabel).collect(Collectors.joining(", "))
					+ ". Informe tieBreakOptionId para desempatar.");
			}
			winner = leaders.stream().filter(option -> option.getId().equals(tieBreakOptionId)).findFirst()
				.orElseThrow(() -> new BusinessException("Opcao de desempate precisa estar entre as empatadas"));
		}
		else {
			winner = leaders.get(0);
		}

		poll.setStatus(PollStatus.CLOSED);
		poll.setClosedAt(LocalDateTime.now());
		poll.setWinningOption(winner);
		MusicalProject project = poll.getProject();
		long total = poll.getVotes().size();
		String result = "\"" + winner.getLabel() + "\" venceu com " + max + " de " + total + " voto(s)"
			+ (tie ? " (desempate)" : "");

		// Registro permanente da decisao.
		Artist decidedBy = actor != null ? actor : poll.getCreatedBy();
		if (decidedBy == null) {
			decidedBy = poll.getVotes().get(0).getArtist();
		}
		DecisionRecord decision = new DecisionRecord();
		decision.setProject(project);
		decision.setTitle(poll.getQuestion());
		decision.setDescription("Decidido por enquete #" + poll.getId() + ": " + result + ". Placar: "
			+ poll.getOptions().stream()
				.map(option -> option.getLabel() + " = " + counts.getOrDefault(option.getId(), 0L))
				.collect(Collectors.joining("; ")));
		decision.setImpact(winner.getFileVersion() == null
			? null
			: "Arquivo \"" + winner.getFileVersion().getFile().getName() + "\" travado na v"
				+ winner.getFileVersion().getVersionNumber());
		decision.setDecidedAt(LocalDate.now());
		decision.setStatus(DecisionStatus.REGISTERED);
		decision.setDecidedByArtist(decidedBy);
		poll.setDecisionRecordId(decisionRepository.save(decision).getId());

		auditLogService.record(project.getId(), "POLL_CLOSED", "Poll", poll.getId(), actor, result);
		if (winner.getFileVersion() != null) {
			fileService.lockAt(winner.getFileVersion(), poll.getId(), actor);
		}
		notificationService.notifyAll(project.getArtists(), null, project.getId(), NotificationType.POLL_CLOSED,
			"Enquete encerrada: " + poll.getQuestion(), result, poll.getId());
	}

	private void ensureOpen(Poll poll) {
		if (poll.getStatus() != PollStatus.OPEN) {
			throw new BusinessException("Enquete encerrada");
		}
		if (poll.getClosesAt() != null && poll.getClosesAt().isBefore(LocalDateTime.now())) {
			throw new BusinessException("Prazo de votacao encerrado");
		}
	}

	private Poll findEntity(Long id) {
		return pollRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Enquete nao encontrada: " + id));
	}

	static PollResponse toResponse(Poll poll) {
		Map<Long, List<PollVote>> byOption = poll.getVotes().stream()
			.collect(Collectors.groupingBy(vote -> vote.getOption().getId()));
		List<PollOptionResponse> options = poll.getOptions().stream()
			.sorted(Comparator.comparingInt(PollOption::getPosition))
			.map(option -> {
				List<PollVote> votes = byOption.getOrDefault(option.getId(), List.of());
				FileVersion version = option.getFileVersion();
				return new PollOptionResponse(
					option.getId(),
					option.getLabel(),
					option.getPosition(),
					version == null ? null : version.getId(),
					version == null ? null : version.getFile().getId(),
					version == null ? null : version.getVersionNumber(),
					version == null ? null : StorageService.MEDIA_PREFIX + version.getStorageKey(),
					votes.size(),
					votes.stream().map(vote -> vote.getArtist().getStageName()).toList());
			})
			.toList();
		return new PollResponse(
			poll.getId(),
			poll.getProject().getId(),
			poll.getQuestion(),
			poll.getDescription(),
			poll.getStatus(),
			poll.getCreatedBy() == null ? null : poll.getCreatedBy().getId(),
			poll.getCreatedBy() == null ? null : poll.getCreatedBy().getStageName(),
			poll.getCreatedAt(),
			poll.getClosesAt(),
			poll.getClosedAt(),
			poll.getWinningOption() == null ? null : poll.getWinningOption().getId(),
			poll.getDecisionRecordId(),
			poll.getVotes().size(),
			options);
	}
}
