package br.edu.ifsp.harmocrew_api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsp.harmocrew_api.entities.ProjectFile;

public interface ProjectFileRepository extends JpaRepository<ProjectFile, Long> {
	List<ProjectFile> findByProjectIdOrderByCreatedAtDesc(Long projectId);
}
