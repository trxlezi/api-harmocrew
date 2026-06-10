package br.edu.ifsp.harmocrew_api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsp.harmocrew_api.entities.CollaborationMessage;

public interface CollaborationMessageRepository extends JpaRepository<CollaborationMessage, Long> {

	List<CollaborationMessage> findByProjectIdOrderBySentAtAsc(Long projectId);
}
