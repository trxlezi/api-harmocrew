package br.edu.ifsp.harmocrew_api.services;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifsp.harmocrew_api.dtos.ApplicationResponse;
import br.edu.ifsp.harmocrew_api.dtos.MatchResponse;
import br.edu.ifsp.harmocrew_api.dtos.MatchResponse.CriterionScore;
import br.edu.ifsp.harmocrew_api.dtos.mapper.ApplicationMapper;
import br.edu.ifsp.harmocrew_api.entities.Application;
import br.edu.ifsp.harmocrew_api.entities.Artist;
import br.edu.ifsp.harmocrew_api.entities.MusicalProject;
import br.edu.ifsp.harmocrew_api.repositories.ApplicationRepository;
import br.edu.ifsp.harmocrew_api.repositories.ArtistRepository;

/*
 * Modulo de correspondencia algoritmica (Funcionalidade 1).
 *
 * Compara o perfil do musico com os requisitos da vaga (projeto) em 4
 * criterios ponderados:
 *   - genero/estilo musical ......... peso 35
 *   - instrumentos/necessidades ..... peso 30
 *   - influencias ................... peso 20
 *   - localizacao ................... peso 15
 *
 * Criterios que a vaga nao especifica (ex.: sem cidade definida) sao ignorados
 * e os pesos restantes sao renormalizados, para nao inflar nem punir a nota.
 * Textos sao comparados sem acento e sem diferenca de maiusculas.
 */
@Service
public class MatchingService {

	static final int WEIGHT_STYLE = 35;
	static final int WEIGHT_INSTRUMENTS = 30;
	static final int WEIGHT_INFLUENCES = 20;
	static final int WEIGHT_LOCATION = 15;

	private final ArtistRepository artistRepository;
	private final ApplicationRepository applicationRepository;
	private final MusicalProjectService projectService;

	public MatchingService(
		ArtistRepository artistRepository,
		ApplicationRepository applicationRepository,
		MusicalProjectService projectService
	) {
		this.artistRepository = artistRepository;
		this.applicationRepository = applicationRepository;
		this.projectService = projectService;
	}

	public record Score(int score, List<CriterionScore> breakdown, Set<String> matchedInstruments,
		Set<String> matchedInfluences) {
	}

	public Score score(Artist artist, MusicalProject project) {
		List<CriterionScore> breakdown = new ArrayList<>();

		// Estilo: 1.0 se o musico toca o estilo da vaga; senao, sobreposicao de palavras.
		boolean hasStyle = project.getMusicalStyle() != null && !project.getMusicalStyle().isBlank();
		double style = hasStyle ? styleSimilarity(project.getMusicalStyle(), artist.getMusicalStyles()) : 0;
		breakdown.add(new CriterionScore("STYLE", WEIGHT_STYLE, pct(style), hasStyle));

		// Instrumentos: fracao das necessidades da vaga cobertas pelo musico.
		Set<String> skills = new LinkedHashSet<>(artist.getInstruments());
		if (artist.getMainSpecialty() != null) {
			skills.add(artist.getMainSpecialty());
		}
		Set<String> matchedInstruments = intersection(project.getNeeds(), skills);
		boolean hasNeeds = !project.getNeeds().isEmpty();
		double instruments = hasNeeds ? (double) matchedInstruments.size() / project.getNeeds().size() : 0;
		breakdown.add(new CriterionScore("INSTRUMENTS", WEIGHT_INSTRUMENTS, pct(instruments), hasNeeds));

		// Influencias: fracao das influencias desejadas que o musico compartilha.
		Set<String> matchedInfluences = intersection(project.getDesiredInfluences(), artist.getInfluences());
		boolean hasInfluences = !project.getDesiredInfluences().isEmpty();
		double influences = hasInfluences
			? (double) matchedInfluences.size() / project.getDesiredInfluences().size()
			: 0;
		breakdown.add(new CriterionScore("INFLUENCES", WEIGHT_INFLUENCES, pct(influences), hasInfluences));

		// Localizacao: mesma cidade = 1.0.
		boolean hasLocation = project.getLocation() != null && !project.getLocation().isBlank();
		double location = hasLocation && normalize(project.getLocation()).equals(normalize(artist.getCity())) ? 1 : 0;
		breakdown.add(new CriterionScore("LOCATION", WEIGHT_LOCATION, pct(location), hasLocation));

		int appliedWeight = breakdown.stream().filter(CriterionScore::applied).mapToInt(CriterionScore::weight).sum();
		double weighted = breakdown.stream().filter(CriterionScore::applied)
			.mapToDouble(criterion -> criterion.weight() * criterion.score() / 100.0).sum();
		int total = appliedWeight == 0 ? 0 : (int) Math.round(weighted * 100 / appliedWeight);
		return new Score(total, breakdown, matchedInstruments, matchedInfluences);
	}

	/*
	 * Ranqueia todos os musicos cadastrados para a vaga, aplicando filtros
	 * opcionais por estilo, instrumento e cidade.
	 */
	@Transactional(readOnly = true)
	public List<MatchResponse> rankArtists(Long projectId, String style, String instrument, String city,
		Integer minScore, boolean includeMembers, Integer limit) {
		MusicalProject project = projectService.findEntity(projectId);
		Map<Long, Application> applicationsByArtist = new HashMap<>();
		applicationRepository.findByProjectId(projectId)
			.forEach(application -> applicationsByArtist.put(application.getArtist().getId(), application));
		Set<Long> memberIds = project.getArtists().stream().map(Artist::getId).collect(Collectors.toSet());

		return artistRepository.findAll().stream()
			.filter(artist -> includeMembers || !memberIds.contains(artist.getId()))
			.filter(artist -> isBlank(style) || containsNormalized(artist.getMusicalStyles(), style))
			.filter(artist -> isBlank(instrument) || containsNormalized(artist.getInstruments(), instrument)
				|| normalize(artist.getMainSpecialty()).equals(normalize(instrument)))
			.filter(artist -> isBlank(city) || normalize(artist.getCity()).equals(normalize(city)))
			.map(artist -> toResponse(artist, score(artist, project), memberIds.contains(artist.getId()),
				applicationsByArtist.get(artist.getId())))
			.filter(match -> minScore == null || match.score() >= minScore)
			.sorted(Comparator.comparingInt(MatchResponse::score).reversed()
				.thenComparing(match -> match.stageName().toLowerCase()))
			.limit(limit == null || limit <= 0 ? Long.MAX_VALUE : limit)
			.toList();
	}

	// Candidaturas da vaga ordenadas pela compatibilidade atual (maior primeiro).
	@Transactional
	public List<ApplicationResponse> rankApplications(Long projectId) {
		MusicalProject project = projectService.findEntity(projectId);
		List<Application> applications = applicationRepository.findByProjectId(projectId);
		applications.forEach(application -> application.setMatchScore(score(application.getArtist(), project).score()));
		return applications.stream()
			.sorted(Comparator.comparing(Application::getMatchScore, Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparing(Application::getCreatedAt))
			.map(ApplicationMapper::toResponse)
			.toList();
	}

	private MatchResponse toResponse(Artist artist, Score score, boolean member, Application application) {
		return new MatchResponse(
			artist.getId(),
			artist.getStageName(),
			artist.getMainSpecialty(),
			artist.getCity(),
			new LinkedHashSet<>(artist.getMusicalStyles()),
			new LinkedHashSet<>(artist.getInstruments()),
			new LinkedHashSet<>(artist.getInfluences()),
			artist.getAudioUrl(),
			score.score(),
			score.breakdown(),
			score.matchedInstruments(),
			score.matchedInfluences(),
			member,
			application == null ? null : application.getId(),
			application == null ? null : application.getStatus());
	}

	private static double styleSimilarity(String wanted, Collection<String> styles) {
		if (containsNormalized(styles, wanted)) {
			return 1;
		}
		Set<String> wantedTokens = tokens(wanted);
		double best = 0;
		for (String style : styles) {
			Set<String> tokens = tokens(style);
			Set<String> union = new LinkedHashSet<>(tokens);
			union.addAll(wantedTokens);
			tokens.retainAll(wantedTokens);
			if (!union.isEmpty()) {
				best = Math.max(best, (double) tokens.size() / union.size());
			}
		}
		return best;
	}

	private static Set<String> tokens(String value) {
		Set<String> tokens = new LinkedHashSet<>();
		for (String token : normalize(value).split("[^a-z0-9]+")) {
			if (!token.isBlank()) {
				tokens.add(token);
			}
		}
		return tokens;
	}

	// Itens de "wanted" que aparecem em "offered" (comparacao normalizada).
	private static Set<String> intersection(Collection<String> wanted, Collection<String> offered) {
		Set<String> normalizedOffered = offered.stream().map(MatchingService::normalize).collect(Collectors.toSet());
		return wanted.stream()
			.filter(item -> normalizedOffered.contains(normalize(item)))
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	static boolean containsNormalized(Collection<String> values, String wanted) {
		String target = normalize(wanted);
		return values.stream().anyMatch(value -> normalize(value).equals(target));
	}

	static String normalize(String value) {
		if (value == null) {
			return "";
		}
		return Normalizer.normalize(value, Normalizer.Form.NFD)
			.replaceAll("\\p{M}", "")
			.toLowerCase()
			.trim();
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private static int pct(double value) {
		return (int) Math.round(value * 100);
	}
}
