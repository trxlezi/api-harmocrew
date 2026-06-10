package br.edu.ifsp.harmocrew_api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsp.harmocrew_api.entities.Application;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

	List<Application> findByProjectId(Long projectId);

	List<Application> findByArtistId(Long artistId);
}
