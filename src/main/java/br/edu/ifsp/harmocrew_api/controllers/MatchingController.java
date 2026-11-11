package br.edu.ifsp.harmocrew_api.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifsp.harmocrew_api.dtos.ApplicationResponse;
import br.edu.ifsp.harmocrew_api.dtos.MatchResponse;
import br.edu.ifsp.harmocrew_api.services.MatchingService;

@RestController
@RequestMapping("/api/projects/{projectId}")
public class MatchingController {

	private final MatchingService matchingService;

	public MatchingController(MatchingService matchingService) {
		this.matchingService = matchingService;
	}

	// Ranking de musicos compativeis com a vaga, com filtros por estilo/instrumento/cidade.
	@GetMapping("/matches")
	public List<MatchResponse> matches(
		@PathVariable Long projectId,
		@RequestParam(required = false) String style,
		@RequestParam(required = false) String instrument,
		@RequestParam(required = false) String city,
		@RequestParam(required = false) Integer minScore,
		@RequestParam(defaultValue = "false") boolean includeMembers,
		@RequestParam(required = false) Integer limit
	) {
		return matchingService.rankArtists(projectId, style, instrument, city, minScore, includeMembers, limit);
	}

	// Candidaturas da vaga ordenadas pela nota de compatibilidade.
	@GetMapping("/applications/ranked")
	public List<ApplicationResponse> rankedApplications(@PathVariable Long projectId) {
		return matchingService.rankApplications(projectId);
	}
}
