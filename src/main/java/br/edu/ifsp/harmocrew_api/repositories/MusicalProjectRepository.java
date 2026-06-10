package br.edu.ifsp.harmocrew_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsp.harmocrew_api.entities.MusicalProject;

public interface MusicalProjectRepository extends JpaRepository<MusicalProject, Long> {
}
