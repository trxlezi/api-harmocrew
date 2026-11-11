package br.edu.ifsp.harmocrew_api.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.ifsp.harmocrew_api.services.TaskService;

/*
 * Cobre as 4 funcionalidades do documento de ideias:
 * 1) matching/ranking  2) versionamento + enquetes + auditoria
 * 3) workflow com etapas, timeline e alertas  4) dashboard de metricas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NewFeaturesIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TaskService taskService;

	private final ObjectMapper objectMapper = new ObjectMapper();
	private String token;

	@BeforeEach
	void login() throws Exception {
		MvcResult result = mockMvc.perform(post("/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"name":"Gestor","email":"%s","password":"secret123","stageName":"Gestor","mainSpecialty":"Producao"}
					""".formatted("u" + UUID.randomUUID() + "@harmocrew.test")))
			.andExpect(status().isCreated())
			.andReturn();
		token = json(result).get("token").asText();
	}

	// ------------------------------------------------------------ Funcionalidade 1

	@Test
	void shouldRankArtistsByProfileSimilarityAndGiveCandidateFeedback() throws Exception {
		Long best = createArtist("Ana Groove", "[\"baixo\"]", "[\"Samba Rock\", \"MPB\"]", "Sao Paulo", "[\"Jorge Ben\", \"Tim Maia\"]");
		Long medium = createArtist("Beto Rock", "[\"baixo\"]", "[\"rock\"]", "Campinas", "[\"Tim Maia\"]");
		Long weak = createArtist("Caio Jazz", "[\"piano\"]", "[\"jazz\"]", "Recife", "[]");
		Long projectId = createProject("Vaga Baixista", "samba rock", "[\"Baixo\"]", "São Paulo", "[\"jorge ben\", \"tim maia\"]");

		JsonNode ranking = json(perform(get("/api/projects/{id}/matches", projectId)).andExpect(status().isOk()).andReturn());
		assertThat(positionOf(ranking, best)).isLessThan(positionOf(ranking, medium));
		assertThat(positionOf(ranking, medium)).isLessThan(positionOf(ranking, weak));
		assertThat(scoreOf(ranking, best)).isEqualTo(100);
		assertThat(scoreOf(ranking, weak)).isZero();

		perform(get("/api/projects/{id}/matches", projectId).param("style", "jazz"))
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].artistId").value(weak));
		perform(get("/api/artists").param("instrument", "BAIXO").param("city", "sao paulo"))
			.andExpect(jsonPath("$[*].id", hasItem(best.intValue())));

		// Perfil com audio (link imutavel publico).
		MockMultipartFile audio = new MockMultipartFile("file", "demo.mp3", "audio/mpeg", "ID3-fake-audio".getBytes());
		MvcResult upload = perform(multipart("/api/artists/{id}/audio", best).file(audio)).andExpect(status().isOk()).andReturn();
		String audioUrl = json(upload).get("audioUrl").asText();
		assertThat(audioUrl).startsWith("/media/");
		mockMvc.perform(get(audioUrl)).andExpect(status().isOk()).andExpect(content().bytes("ID3-fake-audio".getBytes()));

		// Candidatura recebe nota e o candidato e notificado a cada mudanca de status.
		Long applicationId = postForId("/api/projects/" + projectId + "/applications", """
			{"artistId": %d, "message": "Quero tocar", "specialty": "Baixo", "availability": "Noites"}
			""".formatted(best));
		postForId("/api/projects/" + projectId + "/applications", """
			{"artistId": %d, "message": "Tambem quero", "specialty": "Baixo", "availability": "Noites"}
			""".formatted(medium));
		perform(get("/api/projects/{id}/applications/ranked", projectId))
			.andExpect(jsonPath("$[0].artistId").value(best))
			.andExpect(jsonPath("$[0].matchScore").value(100))
			.andExpect(jsonPath("$[0].artistAudioUrl").value(audioUrl));

		perform(json(patch("/api/applications/{id}/status", applicationId),
			"{\"status\":\"UNDER_REVIEW\",\"evaluationNote\":\"Ouvindo seu audio\"}"))
			.andExpect(jsonPath("$.status").value("UNDER_REVIEW"))
			.andExpect(jsonPath("$.evaluationNote").value("Ouvindo seu audio"));
		perform(get("/api/notifications").param("artistId", best.toString()))
			.andExpect(jsonPath("$[0].type").value("APPLICATION_STATUS_CHANGED"))
			.andExpect(jsonPath("$[0].body", containsString("Em avaliacao")));

		// Aprovar adiciona o musico ao projeto.
		perform(json(patch("/api/applications/{id}/status", applicationId), "{\"status\":\"APPROVED\"}"));
		perform(get("/api/projects/{id}", projectId)).andExpect(jsonPath("$.artists[*].id", hasItem(best.intValue())));
	}

	// ------------------------------------------------------------ Funcionalidade 2

	@Test
	void shouldVersionFilesDecideByPollAndLockDecidedVersion() throws Exception {
		Long ana = createArtist("Ana", "[\"voz\"]", "[\"mpb\"]", "Sao Paulo", "[]");
		Long bia = createArtist("Bia", "[\"guitarra\"]", "[\"mpb\"]", "Sao Paulo", "[]");
		Long caio = createArtist("Caio", "[\"bateria\"]", "[\"mpb\"]", "Sao Paulo", "[]");
		Long projectId = createProject("Show de Verao", "mpb", "[]", null, "[]");
		for (Long member : new Long[] {ana, bia, caio}) {
			perform(post("/api/projects/{p}/artists/{a}", projectId, member)).andExpect(status().isOk());
		}

		MvcResult created = perform(multipart("/api/projects/{id}/files", projectId)
				.file(new MockMultipartFile("file", "arranjo.pdf", "application/pdf", "arranjo v1".getBytes()))
				.param("name", "Arranjo Asa Branca").param("category", "ARRANGEMENT").param("artistId", ana.toString()))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.currentVersion.versionNumber").value(1))
			.andExpect(jsonPath("$.currentVersion.uploadedByName").value("Ana"))
			.andReturn();
		Long fileId = json(created).get("id").asLong();
		String v1Url = json(created).get("currentVersion").get("url").asText();

		MvcResult second = perform(multipart("/api/files/{id}/versions", fileId)
				.file(new MockMultipartFile("file", "arranjo.pdf", "application/pdf", "arranjo v2".getBytes()))
				.param("note", "Ponte reescrita").param("artistId", bia.toString()))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.versions.length()").value(2))
			.andExpect(jsonPath("$.currentVersion.versionNumber").value(2))
			.andReturn();
		Long v1Id = json(second).get("versions").get(1).get("id").asLong();
		Long v2Id = json(second).get("versions").get(0).get("id").asLong();

		// Conteudo identico nao gera versao nova; links antigos continuam validos.
		perform(multipart("/api/files/{id}/versions", fileId)
				.file(new MockMultipartFile("file", "x.pdf", "application/pdf", "arranjo v2".getBytes())))
			.andExpect(status().isBadRequest());
		mockMvc.perform(get(v1Url)).andExpect(content().bytes("arranjo v1".getBytes()));
		perform(get("/api/files/{id}/versions/1", fileId)).andExpect(jsonPath("$.url").value(v1Url));

		MvcResult pollResult = perform(json(post("/api/projects/{id}/polls", projectId), """
			{"question":"Qual arranjo vamos tocar?","createdByArtistId":%d,
			 "options":[{"label":"Versao 1","fileVersionId":%d},{"label":"Versao 2","fileVersionId":%d}]}
			""".formatted(ana, v1Id, v2Id)))
			.andExpect(status().isCreated())
			.andReturn();
		JsonNode poll = json(pollResult);
		Long pollId = poll.get("id").asLong();
		Long optionV1 = poll.get("options").get(0).get("id").asLong();
		Long optionV2 = poll.get("options").get(1).get("id").asLong();

		vote(pollId, ana, optionV2);
		vote(pollId, bia, optionV1);
		vote(pollId, caio, optionV1);
		vote(pollId, bia, optionV2); // troca de voto: 2 x 1 para a versao 2
		perform(json(post("/api/polls/{id}/votes", pollId), "{\"artistId\":%d,\"optionId\":%d}"
				.formatted(createArtist("Intruso", "[]", "[]", null, "[]"), optionV1)))
			.andExpect(status().isBadRequest());

		perform(json(post("/api/polls/{id}/close", pollId), "{\"artistId\":%d}".formatted(ana)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("CLOSED"))
			.andExpect(jsonPath("$.winningOptionId").value(optionV2))
			.andExpect(jsonPath("$.decisionRecordId").isNumber());

		perform(get("/api/files/{id}", fileId))
			.andExpect(jsonPath("$.locked").value(true))
			.andExpect(jsonPath("$.lockedVersionNumber").value(2));
		perform(multipart("/api/files/{id}/versions", fileId)
				.file(new MockMultipartFile("file", "v3.pdf", "application/pdf", "arranjo v3".getBytes())))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message", containsString("travado")));
		perform(json(post("/api/polls/{id}/votes", pollId), "{\"artistId\":%d,\"optionId\":%d}".formatted(caio, optionV2)))
			.andExpect(status().isBadRequest());

		perform(get("/api/projects/{id}/decisions", projectId))
			.andExpect(jsonPath("$[0].title").value("Qual arranjo vamos tocar?"));
		JsonNode audit = json(perform(get("/api/projects/{id}/audit-logs", projectId)).andReturn());
		assertThat(audit.findValuesAsText("action"))
			.contains("FILE_CREATED", "FILE_VERSION_UPLOADED", "POLL_OPENED", "POLL_VOTE", "POLL_VOTE_CHANGED", "POLL_CLOSED", "FILE_LOCKED");

		perform(json(post("/api/files/{id}/unlock", fileId), "{\"artistId\":%d,\"reason\":\"Mudanca de tom\"}".formatted(ana)))
			.andExpect(jsonPath("$.locked").value(false));
	}

	// ------------------------------------------------------- Funcionalidades 3 e 4

	@Test
	void shouldTrackWorkflowStagesAlertMembersAndComputeDashboard() throws Exception {
		Long ana = createArtist("Ana Prod", "[\"voz\"]", "[\"pop\"]", "Sao Paulo", "[]");
		Long bia = createArtist("Bia Prod", "[\"guitarra\"]", "[\"pop\"]", "Sao Paulo", "[]");
		Long projectId = createProject("Single Novo", "pop", "[]", null, "[]");
		perform(post("/api/projects/{p}/artists/{a}", projectId, ana));
		perform(post("/api/projects/{p}/artists/{a}", projectId, bia));
		LocalDate today = LocalDate.now();

		Long composition = postForId("/api/projects/" + projectId + "/tasks",
			task("Compor letra", "DONE", "COMPOSITION", today.minusDays(10), today.minusDays(3), ana));
		Long arrangement = postForId("/api/projects/" + projectId + "/tasks",
			task("Arranjo de cordas", "IN_PROGRESS", "ARRANGEMENT", today.minusDays(5), today.minusDays(1), bia));
		postForId("/api/projects/" + projectId + "/tasks",
			task("Gravar voz", "TODO", "RECORDING", today.plusDays(2), today.plusDays(7), null));

		perform(get("/api/notifications").param("artistId", bia.toString()))
			.andExpect(jsonPath("$[*].type", hasItem("TASK_ASSIGNED")));

		// Mudanca de status alerta o grupo.
		perform(json(patch("/api/tasks/{id}/status", arrangement), "{\"status\":\"DONE\"}"));
		perform(get("/api/notifications").param("artistId", ana.toString()).param("unreadOnly", "true"))
			.andExpect(jsonPath("$[*].type", hasItem("TASK_STATUS_CHANGED")));
		perform(json(patch("/api/tasks/{id}/status", arrangement), "{\"status\":\"IN_PROGRESS\"}"));

		// Verificacao de prazo estourado (alerta uma unica vez).
		assertThat(taskService.alertOverdueTasks()).isGreaterThanOrEqualTo(1);
		perform(get("/api/notifications").param("artistId", bia.toString()))
			.andExpect(jsonPath("$[*].type", hasItem("TASK_OVERDUE")));
		long overdueAlerts = json(perform(get("/api/notifications").param("artistId", bia.toString())).andReturn())
			.findValuesAsText("type").stream().filter("TASK_OVERDUE"::equals).count();
		taskService.alertOverdueTasks();
		assertThat(json(perform(get("/api/notifications").param("artistId", bia.toString())).andReturn())
			.findValuesAsText("type").stream().filter("TASK_OVERDUE"::equals).count()).isEqualTo(overdueAlerts);

		perform(patch("/api/notifications/read-all").param("artistId", ana.toString())).andExpect(status().isNoContent());
		perform(get("/api/notifications").param("artistId", ana.toString()).param("unreadOnly", "true"))
			.andExpect(jsonPath("$.length()").value(0));

		perform(get("/api/projects/{id}/timeline", projectId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.stages.length()").value(5))
			.andExpect(jsonPath("$.stages[0].progressPercent").value(100))
			.andExpect(jsonPath("$.stages[1].overdueTasks").value(1))
			.andExpect(jsonPath("$.currentStage").value("ARRANGEMENT"))
			.andExpect(jsonPath("$.stages[0].tasks[0].id").value(composition));

		// Assiduidade: Ana foi nos 2 ensaios, Bia em 1 -> 75%.
		for (int i = 0; i < 2; i++) {
			Long rehearsal = postForId("/api/projects/" + projectId + "/rehearsals", """
				{"title":"Ensaio %d","date":"%s","time":"19:00:00","location":"Estudio","status":"SCHEDULED",
				 "participantArtistIds":[%d,%d]}
				""".formatted(i, today.minusDays(2 + i), ana, bia));
			String attended = i == 0 ? "[%d,%d]".formatted(ana, bia) : "[%d]".formatted(ana);
			perform(json(patch("/api/rehearsals/{id}/attendance", rehearsal), "{\"attendedArtistIds\":" + attended + "}"))
				.andExpect(jsonPath("$.status").value("COMPLETED"));
		}

		perform(get("/api/projects/{id}/dashboard", projectId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalTasks").value(3))
			.andExpect(jsonPath("$.doneTasks").value(1))
			.andExpect(jsonPath("$.completionPercent").value(33))
			.andExpect(jsonPath("$.overdueTasks").value(1))
			.andExpect(jsonPath("$.attendancePercent").value(75))
			.andExpect(jsonPath("$.tasksByStage.RECORDING").value(1))
			.andExpect(jsonPath("$.weeklyThroughput.length()").value(8))
			.andExpect(jsonPath("$.members[?(@.name == 'Bia Prod')].attendancePercent").value(hasItem(50)))
			.andExpect(jsonPath("$.recentTasks.length()").value(3))
			.andExpect(jsonPath("$.upcomingDeadlines[0].id").value(arrangement));
		perform(get("/api/dashboard").param("artistId", ana.toString()))
			.andExpect(jsonPath("$.scope").value("ARTIST"))
			.andExpect(jsonPath("$.projects.length()").value(greaterThanOrEqualTo(1)));
	}

	// ------------------------------------------------------------------ helpers

	private void vote(Long pollId, Long artistId, Long optionId) throws Exception {
		perform(json(post("/api/polls/{id}/votes", pollId), "{\"artistId\":%d,\"optionId\":%d}".formatted(artistId, optionId)))
			.andExpect(status().isOk());
	}

	private String task(String title, String status, String stage, LocalDate start, LocalDate due, Long assignee) {
		return """
			{"title":"%s","status":"%s","priority":"MEDIUM","responsibleName":"Equipe","stage":"%s",
			 "startDate":"%s","dueDate":"%s","assigneeArtistId":%s}
			""".formatted(title, status, stage, start, due, assignee);
	}

	private Long createArtist(String name, String instruments, String styles, String city, String influences) throws Exception {
		return postForId("/api/artists", """
			{"stageName":"%s","mainSpecialty":"Musico","instruments":%s,"musicalStyles":%s,
			 "availability":"Livre","city":%s,"influences":%s}
			""".formatted(name, instruments, styles, city == null ? "null" : "\"" + city + "\"", influences));
	}

	private Long createProject(String title, String style, String needs, String location, String influences) throws Exception {
		return postForId("/api/projects", """
			{"title":"%s","musicalStyle":"%s","status":"ACTIVE","needs":%s,"startDate":"2026-01-01",
			 "location":%s,"desiredInfluences":%s}
			""".formatted(title, style, needs, location == null ? "null" : "\"" + location + "\"", influences));
	}

	private Long postForId(String path, String body) throws Exception {
		MvcResult result = perform(json(post(path), body)).andExpect(status().isCreated()).andReturn();
		return json(result).get("id").asLong();
	}

	private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder builder, String body) {
		return builder.contentType(MediaType.APPLICATION_JSON).content(body.getBytes(StandardCharsets.UTF_8));
	}

	private org.springframework.test.web.servlet.ResultActions perform(org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder<?> builder) throws Exception {
		return mockMvc.perform(builder.header("Authorization", "Bearer " + token));
	}

	private JsonNode json(MvcResult result) throws Exception {
		return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
	}

	private static int positionOf(JsonNode ranking, Long artistId) {
		for (int i = 0; i < ranking.size(); i++) {
			if (ranking.get(i).get("artistId").asLong() == artistId) {
				return i;
			}
		}
		throw new AssertionError("Artista " + artistId + " fora do ranking");
	}

	private static int scoreOf(JsonNode ranking, Long artistId) {
		return ranking.get(positionOf(ranking, artistId)).get("score").asInt();
	}

}
