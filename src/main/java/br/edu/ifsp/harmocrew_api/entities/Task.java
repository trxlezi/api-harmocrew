package br.edu.ifsp.harmocrew_api.entities;

import java.time.LocalDateTime;

import br.edu.ifsp.harmocrew_api.enums.ProductionStage;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDate;

import br.edu.ifsp.harmocrew_api.enums.TaskPriority;
import br.edu.ifsp.harmocrew_api.enums.TaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tasks")
public class Task {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "text")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TaskStatus status = TaskStatus.TODO;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TaskPriority priority = TaskPriority.MEDIUM;

	private LocalDate dueDate;

	@Column(nullable = false)
	private String responsibleName;

	// Etapa do workflow musical (Composicao -> ... -> Lancamento).
	@Enumerated(EnumType.STRING)
	private ProductionStage stage;

	// Inicio planejado: junto com dueDate forma a barra do Gantt.
	private LocalDate startDate;

	// Responsavel formal (artista). responsibleName continua como texto livre.
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assignee_artist_id")
	private Artist assignee;

	private LocalDateTime completedAt;

	private LocalDateTime updatedAt;

	// Evita alertar o mesmo atraso repetidamente.
	private LocalDateTime overdueNotifiedAt;

	@PrePersist
	@PreUpdate
	void touch() {
		updatedAt = LocalDateTime.now();
	}

	public boolean isOverdue() {
		return status != TaskStatus.DONE && dueDate != null && dueDate.isBefore(LocalDate.now());
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private MusicalProject project;
}
