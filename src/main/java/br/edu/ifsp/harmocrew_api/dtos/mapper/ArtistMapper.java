package br.edu.ifsp.harmocrew_api.dtos.mapper;

import java.util.LinkedHashSet;

import br.edu.ifsp.harmocrew_api.dtos.ArtistResponse;
import br.edu.ifsp.harmocrew_api.dtos.ArtistSummaryResponse;
import br.edu.ifsp.harmocrew_api.entities.Artist;

public final class ArtistMapper {

	private ArtistMapper() {
	}

	public static ArtistSummaryResponse toSummary(Artist artist) {
		if (artist == null) {
			return null;
		}

		return new ArtistSummaryResponse(
			artist.getId(),
			artist.getStageName(),
			artist.getMainSpecialty(),
			artist.getCity());
	}

	public static ArtistResponse toResponse(Artist artist) {
		if (artist == null) {
			return null;
		}

		Long userId = artist.getUser() == null ? null : artist.getUser().getId();

		return new ArtistResponse(
			artist.getId(),
			artist.getStageName(),
			artist.getBio(),
			artist.getMainSpecialty(),
			new LinkedHashSet<>(artist.getInstruments()),
			new LinkedHashSet<>(artist.getMusicalStyles()),
			artist.getAvailability(),
			artist.getCity(),
			userId);
	}
}
