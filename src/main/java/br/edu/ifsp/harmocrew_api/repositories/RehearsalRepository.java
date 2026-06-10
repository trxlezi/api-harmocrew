package br.edu.ifsp.harmocrew_api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsp.harmocrew_api.entities.Rehearsal;

public interface RehearsalRepository extends JpaRepository<Rehearsal, Long> {

	List<Rehearsal> findByProjectIdOrderByDateAscTimeAsc(Long projectId);
}
