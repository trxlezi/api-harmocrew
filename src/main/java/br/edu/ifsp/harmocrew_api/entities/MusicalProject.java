package br.edu.ifsp.harmocrew_api.entities;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

import br.edu.ifsp.harmocrew_api.enums.ProjectStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "musical_projects")
public class MusicalProject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "text")
	private String description;

	@Column(nullable = false)
	private String musicalStyle;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ProjectStatus status = ProjectStatus.PLANNING;

	@ElementCollection
	@CollectionTable(name = "musical_project_needs", joinColumns = @JoinColumn(name = "project_id"))
	@Column(name = "need", nullable = false)
	private Set<String> needs = new LinkedHashSet<>();

	private LocalDate startDate;

	@ManyToMany
	@JoinTable(
		name = "musical_project_artists",
		joinColumns = @JoinColumn(name = "project_id"),
		inverseJoinColumns = @JoinColumn(name = "artist_id"))
	private Set<Artist> artists = new LinkedHashSet<>();

	@OneToMany(mappedBy = "project")
	private Set<Task> tasks = new LinkedHashSet<>();

	@OneToMany(mappedBy = "project")
	private Set<Application> applications = new LinkedHashSet<>();

	@OneToMany(mappedBy = "project")
	private Set<Rehearsal> rehearsals = new LinkedHashSet<>();

	@OneToMany(mappedBy = "project")
	private Set<CollaborationMessage> messages = new LinkedHashSet<>();

	@OneToMany(mappedBy = "project")
	private Set<DecisionRecord> decisions = new LinkedHashSet<>();

	@OneToMany(mappedBy = "project")
	private Set<WeeklyGoal> weeklyGoals = new LinkedHashSet<>();
}
