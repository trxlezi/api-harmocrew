package br.edu.ifsp.harmocrew_api.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsp.harmocrew_api.entities.Poll;
import br.edu.ifsp.harmocrew_api.enums.PollStatus;

public interface PollRepository extends JpaRepository<Poll, Long> {
	List<Poll> findByProjectIdOrderByCreatedAtDesc(Long projectId);

	List<Poll> findByStatusAndClosesAtBefore(PollStatus status, LocalDateTime moment);
}
