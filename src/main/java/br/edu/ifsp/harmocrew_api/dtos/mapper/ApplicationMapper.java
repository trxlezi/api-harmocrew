package br.edu.ifsp.harmocrew_api.dtos.mapper;

import br.edu.ifsp.harmocrew_api.dtos.ApplicationResponse;
import br.edu.ifsp.harmocrew_api.entities.Application;
import br.edu.ifsp.harmocrew_api.entities.Artist;
import br.edu.ifsp.harmocrew_api.entities.MusicalProject;

public final class ApplicationMapper {

	private ApplicationMapper() {
	}

	public static ApplicationResponse toResponse(Application application) {
		if (application == null) {
			return null;
		}

		Artist artist = application.getArtist();
		MusicalProject project = application.getProject();

		return new ApplicationResponse(
			application.getId(),
			application.getMessage(),
			application.getSpecialty(),
			application.getAvailability(),
			application.getStatus(),
			application.getCreatedAt(),
			artist == null ? null : artist.getId(),
			artist == null ? null : artist.getStageName(),
			project == null ? null : project.getId(),
			project == null ? null : project.getTitle());
	}
}
