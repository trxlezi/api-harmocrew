package br.edu.ifsp.harmocrew_api.entities;

import java.time.LocalDateTime;

import br.edu.ifsp.harmocrew_api.enums.CollaborationMessageType;
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
@Table(name = "collaboration_messages")
public class CollaborationMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, columnDefinition = "text")
	private String content;

	@Column(nullable = false)
	private LocalDateTime sentAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CollaborationMessageType type = CollaborationMessageType.MESSAGE;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sender_artist_id", nullable = false)
	private Artist senderArtist;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private MusicalProject project;

	@PrePersist
	void prePersist() {
		if (sentAt == null) {
			sentAt = LocalDateTime.now();
		}
	}
}
