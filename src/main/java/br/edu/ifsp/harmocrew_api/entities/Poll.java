package br.edu.ifsp.harmocrew_api.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.edu.ifsp.harmocrew_api.enums.PollStatus;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/*
 * Enquete para decisao em grupo (ex.: setlist, arranjo). Ao encerrar, o
 * resultado vira um DecisionRecord, entra no log de auditoria e, se a opcao
 * vencedora aponta para uma versao de arquivo, o arquivo e travado nela.
 */
@Getter
@Setter
@Entity
@Table(name = "polls")
public class Poll {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String question;

	@Column(columnDefinition = "text")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PollStatus status = PollStatus.OPEN;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private MusicalProject project;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by_artist_id")
	private Artist createdBy;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	private LocalDateTime closesAt;

	private LocalDateTime closedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "winning_option_id")
	private PollOption winningOption;

	private Long decisionRecordId;

	@OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("position ASC")
	private List<PollOption> options = new ArrayList<>();

	@OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PollVote> votes = new ArrayList<>();

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}
