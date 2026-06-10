package br.edu.ifsp.harmocrew_api.dtos.mapper;

import java.util.LinkedHashSet;

import br.edu.ifsp.harmocrew_api.dtos.RehearsalResponse;
import br.edu.ifsp.harmocrew_api.entities.MusicalProject;
import br.edu.ifsp.harmocrew_api.entities.Rehearsal;

public final class RehearsalMapper {

	private RehearsalMapper() {
	}

	public static RehearsalResponse toResponse(Rehearsal rehearsal) {
		MusicalProject project = rehearsal.getProject();
		return new RehearsalResponse(
			rehearsal.getId(),
			project == null ? null : project.getId(),
			project == null ? null : project.getTitle(),
			rehearsal.getTitle(),
			rehearsal.getDate(),
			rehearsal.getTime(),
			rehearsal.getLocation(),
			new LinkedHashSet<>(rehearsal.getParticipantArtistIds()),
			rehearsal.getNotes(),
			rehearsal.getStatus());
	}
}
