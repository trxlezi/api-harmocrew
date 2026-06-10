package br.edu.ifsp.harmocrew_api.dtos.mapper;

import br.edu.ifsp.harmocrew_api.dtos.DecisionRecordResponse;
import br.edu.ifsp.harmocrew_api.entities.Artist;
import br.edu.ifsp.harmocrew_api.entities.DecisionRecord;
import br.edu.ifsp.harmocrew_api.entities.MusicalProject;

public final class DecisionRecordMapper {

	private DecisionRecordMapper() {
	}

	public static DecisionRecordResponse toResponse(DecisionRecord decision) {
		Artist artist = decision.getDecidedByArtist();
		MusicalProject project = decision.getProject();
		return new DecisionRecordResponse(
			decision.getId(),
			project == null ? null : project.getId(),
			project == null ? null : project.getTitle(),
			decision.getTitle(),
			decision.getDescription(),
			artist == null ? null : artist.getId(),
			artist == null ? null : artist.getStageName(),
			decision.getDecidedAt(),
			decision.getImpact(),
			decision.getStatus());
	}
}
