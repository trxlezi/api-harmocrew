package br.edu.ifsp.harmocrew_api.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/*
 * Versao imutavel de um arquivo: numero sequencial, autor, data e hash SHA-256.
 * O conteudo fica em /media/{storageKey}, link que nunca muda.
 */
@Getter
@Setter
@Entity
@Table(name = "file_versions", uniqueConstraints = @UniqueConstraint(columnNames = {"file_id", "version_number"}))
public class FileVersion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "file_id", nullable = false)
	private ProjectFile file;

	@Column(name = "version_number", nullable = false, updatable = false)
	private int versionNumber;

	@Column(nullable = false, updatable = false, unique = true)
	private String storageKey;

	@Column(nullable = false, updatable = false)
	private String originalFilename;

	@Column(updatable = false)
	private String contentType;

	@Column(updatable = false)
	private long sizeBytes;

	@Column(nullable = false, updatable = false, length = 64)
	private String sha256;

	@Column(columnDefinition = "text", updatable = false)
	private String note;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "uploaded_by_artist_id", updatable = false)
	private Artist uploadedBy;

	@Column(nullable = false, updatable = false)
	private LocalDateTime uploadedAt;

	@PrePersist
	void prePersist() {
		if (uploadedAt == null) {
			uploadedAt = LocalDateTime.now();
		}
	}
}
