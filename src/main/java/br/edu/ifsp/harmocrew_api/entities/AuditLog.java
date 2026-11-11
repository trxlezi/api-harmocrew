package br.edu.ifsp.harmocrew_api.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/*
 * Log de auditoria: registro somente-inclusao (append-only) do que aconteceu
 * no projeto. Nao existe endpoint de alteracao ou remocao.
 */
@Getter
@Setter
@Entity
@Table(name = "audit_logs")
public class AuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long projectId;

	@Column(nullable = false)
	private String action;

	@Column(nullable = false)
	private String entityType;

	private Long entityId;

	private Long actorArtistId;

	private String actorName;

	@Column(columnDefinition = "text")
	private String details;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}
