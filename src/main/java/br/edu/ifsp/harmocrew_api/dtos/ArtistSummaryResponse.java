package br.edu.ifsp.harmocrew_api.dtos;

public record ArtistSummaryResponse(
	Long id,
	String stageName,
	String mainSpecialty,
	String city
) {
}
