package br.edu.ifsp.harmocrew_api.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifsp.harmocrew_api.dtos.AuditLogResponse;
import br.edu.ifsp.harmocrew_api.entities.Artist;
import br.edu.ifsp.harmocrew_api.entities.AuditLog;
import br.edu.ifsp.harmocrew_api.repositories.AuditLogRepository;

@Service
public class AuditLogService {

	private final AuditLogRepository repository;

	public AuditLogService(AuditLogRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public void record(Long projectId, String action, String entityType, Long entityId, Artist actor, String details) {
		AuditLog log = new AuditLog();
		log.setProjectId(projectId);
		log.setAction(action);
		log.setEntityType(entityType);
		log.setEntityId(entityId);
		if (actor != null) {
			log.setActorArtistId(actor.getId());
			log.setActorName(actor.getStageName());
		}
		log.setDetails(details);
		repository.save(log);
	}

	@Transactional(readOnly = true)
	public List<AuditLogResponse> listByProject(Long projectId) {
		return repository.findByProjectIdOrderByCreatedAtDescIdDesc(projectId).stream()
			.map(AuditLogService::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<AuditLogResponse> listByEntity(String entityType, Long entityId) {
		return repository.findByEntityTypeAndEntityIdOrderByCreatedAtDescIdDesc(entityType, entityId).stream()
			.map(AuditLogService::toResponse).toList();
	}

	static AuditLogResponse toResponse(AuditLog log) {
		return new AuditLogResponse(log.getId(), log.getProjectId(), log.getAction(), log.getEntityType(),
			log.getEntityId(), log.getActorArtistId(), log.getActorName(), log.getDetails(), log.getCreatedAt());
	}
}
