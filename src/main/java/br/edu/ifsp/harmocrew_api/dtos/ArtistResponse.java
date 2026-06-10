package br.edu.ifsp.harmocrew_api.dtos;

import java.util.Set;

public record ArtistResponse(
	Long id,
	String stageName,
	String bio,
	String mainSpecialty,
	Set<String> instruments,
	Set<String> musicalStyles,
	String availability,
	String city,
	Long userId
) {
}
