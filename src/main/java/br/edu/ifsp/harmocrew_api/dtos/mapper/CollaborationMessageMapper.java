package br.edu.ifsp.harmocrew_api.dtos.mapper;

import br.edu.ifsp.harmocrew_api.dtos.CollaborationMessageResponse;
import br.edu.ifsp.harmocrew_api.entities.Artist;
import br.edu.ifsp.harmocrew_api.entities.CollaborationMessage;
import br.edu.ifsp.harmocrew_api.entities.MusicalProject;

public final class CollaborationMessageMapper {

	private CollaborationMessageMapper() {
	}

	public static CollaborationMessageResponse toResponse(CollaborationMessage message) {
		Artist sender = message.getSenderArtist();
		MusicalProject project = message.getProject();
		return new CollaborationMessageResponse(
			message.getId(),
			project == null ? null : project.getId(),
			project == null ? null : project.getTitle(),
			sender == null ? null : sender.getId(),
			sender == null ? null : sender.getStageName(),
			message.getContent(),
			message.getSentAt(),
			message.getType());
	}
}
