package br.edu.ifsp.harmocrew_api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsp.harmocrew_api.entities.FileVersion;

public interface FileVersionRepository extends JpaRepository<FileVersion, Long> {
	List<FileVersion> findTop10ByFileProjectIdOrderByUploadedAtDesc(Long projectId);
}
