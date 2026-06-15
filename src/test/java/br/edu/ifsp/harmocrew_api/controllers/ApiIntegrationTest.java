package br.edu.ifsp.harmocrew_api.controllers;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldRegisterAndLogin() throws Exception {
		String email = uniqueEmail();
		String password = "secret123";

		mockMvc.perform(post("/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(registerBody(email, password)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.token").value(notNullValue()))
			.andExpect(jsonPath("$.userId").value(notNullValue()))
			.andExpect(jsonPath("$.password").doesNotExist());

		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginBody(email, password)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.token").value(notNullValue()))
			.andExpect(jsonPath("$.email").value(email));
	}

	@Test
	void shouldRejectProtectedRouteWithoutToken() throws Exception {
		mockMvc.perform(get("/api/artists"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void shouldCreateReadUpdateAndDeleteArtist() throws Exception {
		String token = registerAndReturnToken();

		Long artistId = postForId("/api/artists", artistBody("Lia Voz"), token);

		mockMvc.perform(get("/api/artists/{id}", artistId)
				.header("Authorization", bearer(token)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.stageName").value("Lia Voz"));

		mockMvc.perform(put("/api/artists/{id}", artistId)
				.header("Authorization", bearer(token))
				.contentType(MediaType.APPLICATION_JSON)
				.content(artistBody("Lia Harmonia")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.stageName").value("Lia Harmonia"));

		mockMvc.perform(delete("/api/artists/{id}", artistId)
				.header("Authorization", bearer(token)))
			.andExpect(status().isNoContent());
	}

	@Test
	void shouldManageProjectTaskAndApplicationFlow() throws Exception {
		String token = registerAndReturnToken();
		Long artistId = postForId("/api/artists", artistBody("Noah Baixo"), token);
		Long projectId = postForId("/api/projects", projectBody("Sessao Aurora"), token);

		mockMvc.perform(get("/api/projects/{id}", projectId)
				.header("Authorization", bearer(token)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value("Sessao Aurora"));

		mockMvc.perform(put("/api/projects/{id}", projectId)
				.header("Authorization", bearer(token))
				.contentType(MediaType.APPLICATION_JSON)
				.content(projectBody("Sessao Aurora Deluxe")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value("Sessao Aurora Deluxe"));

		Long taskId = postForId("/api/projects/" + projectId + "/tasks", taskBody("Gravar guia"), token);

		mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
				.header("Authorization", bearer(token)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").value(taskId));

		mockMvc.perform(patch("/api/tasks/{id}/status", taskId)
				.header("Authorization", bearer(token))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"status\":\"DONE\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("DONE"));

		Long applicationId = postForId("/api/projects/" + projectId + "/applications", applicationBody(artistId), token);

		mockMvc.perform(patch("/api/applications/{id}/status", applicationId)
				.header("Authorization", bearer(token))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"status\":\"APPROVED\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("APPROVED"));
	}

	private String registerAndReturnToken() throws Exception {
		String email = uniqueEmail();
		MvcResult result = mockMvc.perform(post("/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(registerBody(email, "secret123")))
			.andExpect(status().isCreated())
			.andReturn();

		return json(result).get("token").asText();
	}

	private Long postForId(String path, String body, String token) throws Exception {
		MvcResult result = mockMvc.perform(post(path)
				.header("Authorization", bearer(token))
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").value(notNullValue()))
			.andReturn();

		return json(result).get("id").asLong();
	}

	private JsonNode json(MvcResult result) throws Exception {
		return objectMapper.readTree(result.getResponse().getContentAsString());
	}

	private String uniqueEmail() {
		return "user-" + UUID.randomUUID() + "@harmocrew.test";
	}

	private String bearer(String token) {
		return "Bearer " + token;
	}

	private String registerBody(String email, String password) {
		return """
			{
			  "name": "Usuario Teste",
			  "email": "%s",
			  "password": "%s",
			  "stageName": "Teste Voz",
			  "mainSpecialty": "Vocal"
			}
			""".formatted(email, password);
	}

	private String loginBody(String email, String password) {
		return """
			{
			  "email": "%s",
			  "password": "%s"
			}
			""".formatted(email, password);
	}

	private String artistBody(String stageName) {
		return """
			{
			  "stageName": "%s",
			  "bio": "Musico de teste",
			  "mainSpecialty": "Baixo",
			  "instruments": ["baixo", "violao"],
			  "musicalStyles": ["rock", "mpb"],
			  "availability": "Fins de semana",
			  "city": "Sao Paulo"
			}
			""".formatted(stageName);
	}

	private String projectBody(String title) {
		return """
			{
			  "title": "%s",
			  "description": "Projeto de integracao",
			  "musicalStyle": "MPB",
			  "status": "ACTIVE",
			  "needs": ["baixo", "bateria"],
			  "startDate": "2026-07-01"
			}
			""".formatted(title);
	}

	private String taskBody(String title) {
		return """
			{
			  "title": "%s",
			  "description": "Preparar material",
			  "status": "TODO",
			  "priority": "HIGH",
			  "dueDate": "2026-07-10",
			  "responsibleName": "Noah"
			}
			""".formatted(title);
	}

	private String applicationBody(Long artistId) {
		return """
			{
			  "artistId": %d,
			  "message": "Tenho interesse em participar.",
			  "specialty": "Baixo",
			  "availability": "Fins de semana"
			}
			""".formatted(artistId);
	}
}
