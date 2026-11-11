package br.edu.ifsp.harmocrew_api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsp.harmocrew_api.entities.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

	List<Task> findByProjectId(Long projectId);

	List<Task> findByStatusNotAndDueDateBeforeAndOverdueNotifiedAtIsNull(
		br.edu.ifsp.harmocrew_api.enums.TaskStatus status, java.time.LocalDate date);

	List<Task> findTop10ByProjectIdOrderByUpdatedAtDesc(Long projectId);
}
