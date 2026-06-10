package br.edu.ifsp.harmocrew_api.entities;

import java.time.LocalDate;

import br.edu.ifsp.harmocrew_api.enums.DecisionStatus;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "decision_records")
public class DecisionRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, columnDefinition = "text")
	private String description;

	@Column(columnDefinition = "text")
	private String impact;

	@Column(nullable = false)
	private LocalDate decidedAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DecisionStatus status = DecisionStatus.REGISTERED;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "decided_by_artist_id", nullable = false)
	private Artist decidedByArtist;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private MusicalProject project;

	@PrePersist
	void prePersist() {
		if (decidedAt == null) {
			decidedAt = LocalDate.now();
		}
	}
}
