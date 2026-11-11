package br.edu.ifsp.harmocrew_api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsp.harmocrew_api.entities.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
	List<Notification> findByRecipientArtistIdOrderByCreatedAtDescIdDesc(Long artistId);

	List<Notification> findByRecipientArtistIdAndReadFalseOrderByCreatedAtDescIdDesc(Long artistId);

	long countByRecipientArtistIdAndReadFalse(Long artistId);
}
