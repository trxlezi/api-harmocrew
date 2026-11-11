package br.edu.ifsp.harmocrew_api.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "poll_options")
public class PollOption {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "poll_id", nullable = false)
	private Poll poll;

	@Column(nullable = false)
	private String label;

	private int position;

	// Opcional: a opcao representa uma versao especifica de um arquivo.
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "file_version_id")
	private FileVersion fileVersion;
}
