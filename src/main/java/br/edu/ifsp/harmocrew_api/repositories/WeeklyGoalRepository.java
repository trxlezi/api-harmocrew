package br.edu.ifsp.harmocrew_api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsp.harmocrew_api.entities.WeeklyGoal;

public interface WeeklyGoalRepository extends JpaRepository<WeeklyGoal, Long> {

	List<WeeklyGoal> findByProjectId(Long projectId);

	List<WeeklyGoal> findByOwnerArtistId(Long ownerArtistId);
}
