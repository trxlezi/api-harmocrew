package br.edu.ifsp.harmocrew_api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsp.harmocrew_api.entities.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
	List<AuditLog> findByProjectIdOrderByCreatedAtDescIdDesc(Long projectId);

	List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDescIdDesc(String entityType, Long entityId);
}
