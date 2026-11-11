package br.edu.ifsp.harmocrew_api.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.edu.ifsp.harmocrew_api.enums.FileCategory;
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
 * Documento versionado do projeto (guia de audio, arranjo, partitura, setlist).
 * Cada upload cria uma nova FileVersion; versoes antigas nunca sao apagadas.
 * Quando uma enquete decide uma versao, o arquivo fica "travado" nela.
 */
@Getter
@Setter
@Entity
@Table(name = "project_files")
public class ProjectFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private FileCategory category = FileCategory.OTHER;

	@Column(columnDefinition = "text")
	private String description;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by_artist_id")
	private Artist createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private MusicalProject project;

	@Column(nullable = false)
	private boolean locked;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "locked_version_id")
	private FileVersion lockedVersion;

	private Long lockedByPollId;

	private LocalDateTime lockedAt;

	@OneToMany(mappedBy = "file", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("versionNumber ASC")
	private List<FileVersion> versions = new ArrayList<>();

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	public FileVersion latestVersion() {
		return versions.isEmpty() ? null : versions.get(versions.size() - 1);
	}
}
