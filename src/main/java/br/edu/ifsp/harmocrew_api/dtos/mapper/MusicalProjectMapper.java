package br.edu.ifsp.harmocrew_api.dtos.mapper;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

import br.edu.ifsp.harmocrew_api.dtos.ArtistSummaryResponse;
import br.edu.ifsp.harmocrew_api.dtos.MusicalProjectResponse;
import br.edu.ifsp.harmocrew_api.dtos.MusicalProjectSummaryResponse;
import br.edu.ifsp.harmocrew_api.entities.MusicalProject;

public final class MusicalProjectMapper {

	private MusicalProjectMapper() {
	}

	public static MusicalProjectSummaryResponse toSummary(MusicalProject project) {
		if (project == null) {
			return null;
		}

		return new MusicalProjectSummaryResponse(
			project.getId(),
			project.getTitle(),
			project.getMusicalStyle(),
			project.getStatus());
	}

	public static MusicalProjectResponse toResponse(MusicalProject project) {
		if (project == null) {
			return null;
		}

		List<ArtistSummaryResponse> artists = project.getArtists().stream()
			.sorted(Comparator.comparing(artist -> artist.getStageName().toLowerCase()))
			.map(ArtistMapper::toSummary)
			.toList();

		return new MusicalProjectResponse(
			project.getId(),
			project.getTitle(),
			project.getDescription(),
			project.getMusicalStyle(),
			project.getStatus(),
			new LinkedHashSet<>(project.getNeeds()),
			project.getStartDate(),
			artists);
	}
}
