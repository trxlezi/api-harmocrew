package br.edu.ifsp.harmocrew_api.entities;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

import br.edu.ifsp.harmocrew_api.enums.RehearsalStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
@Table(name = "rehearsals")
public class Rehearsal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private LocalDate date;

	@Column(nullable = false)
	private LocalTime time;

	@Column(nullable = false)
	private String location;

	@Column(columnDefinition = "text")
	private String notes;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private RehearsalStatus status = RehearsalStatus.SCHEDULED;

	@ElementCollection
	@CollectionTable(name = "rehearsal_participants", joinColumns = @JoinColumn(name = "rehearsal_id"))
	@Column(name = "artist_id", nullable = false)
	private Set<Long> participantArtistIds = new LinkedHashSet<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private MusicalProject project;
}
