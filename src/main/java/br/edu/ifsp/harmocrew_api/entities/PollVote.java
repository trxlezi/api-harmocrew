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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

// Um voto por artista por enquete (pode trocar enquanto a enquete esta aberta).
@Getter
@Setter
@Entity
@Table(name = "poll_votes", uniqueConstraints = @UniqueConstraint(columnNames = {"poll_id", "artist_id"}))
public class PollVote {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "poll_id", nullable = false)
	private Poll poll;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "option_id", nullable = false)
	private PollOption option;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "artist_id", nullable = false)
	private Artist artist;

	@Column(nullable = false)
	private LocalDateTime votedAt;
}
