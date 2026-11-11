package br.edu.ifsp.harmocrew_api.entities;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "artists")
public class Artist {

	/*
	 * Entidade do perfil artistico.
	 *
	 * Um Artist pode estar ligado a um User, participar de varios projetos e ter
	 * varias candidaturas. E esse id de Artist que o mobile usa como autoria em
	 * mensagens, metas e candidaturas.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String stageName;

	@Column(columnDefinition = "text")
	private String bio;

	@Column(nullable = false)
	private String mainSpecialty;

	@ElementCollection
	@CollectionTable(name = "artist_instruments", joinColumns = @JoinColumn(name = "artist_id"))
	@Column(name = "instrument", nullable = false)
	private Set<String> instruments = new LinkedHashSet<>();

	@ElementCollection
	@CollectionTable(name = "artist_musical_styles", joinColumns = @JoinColumn(name = "artist_id"))
	@Column(name = "musical_style", nullable = false)
	private Set<String> musicalStyles = new LinkedHashSet<>();

	@Column(nullable = false)
	private String availability;

	private String city;

	// Influencias musicais (artistas/bandas) usadas no calculo de similaridade.
	@ElementCollection
	@CollectionTable(name = "artist_influences", joinColumns = @JoinColumn(name = "artist_id"))
	@Column(name = "influence", nullable = false)
	private Set<String> influences = new LinkedHashSet<>();

	// Link imutavel (/media/{chave}) do audio de apresentacao do perfil.
	private String audioUrl;

	private String audioFilename;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", unique = true)
	private User user;

	// Muitos-para-muitos: varios artistas podem estar no mesmo projeto.
	@ManyToMany(mappedBy = "artists")
	private Set<MusicalProject> projects = new LinkedHashSet<>();

	@OneToMany(mappedBy = "artist")
	private Set<Application> applications = new LinkedHashSet<>();
}
