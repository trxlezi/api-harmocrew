package br.edu.ifsp.harmocrew_api.dtos;

import java.util.List;
import java.util.Set;

import br.edu.ifsp.harmocrew_api.enums.ApplicationStatus;

/*
 * Resultado do ranqueamento de um musico para uma vaga (projeto).
 * score vai de 0 a 100; breakdown mostra a nota (0-100) de cada criterio
 * considerado, para o contratante entender o porque da posicao.
 */
public record MatchResponse(
	Long artistId,
	String stageName,
	String mainSpecialty,
	String city,
	Set<String> musicalStyles,
	Set<String> instruments,
	Set<String> influences,
	String audioUrl,
	int score,
	List<CriterionScore> breakdown,
	Set<String> matchedInstruments,
	Set<String> matchedInfluences,
	boolean member,
	Long applicationId,
	ApplicationStatus applicationStatus
) {

	public record CriterionScore(String criterion, int weight, int score, boolean applied) {
	}
}
