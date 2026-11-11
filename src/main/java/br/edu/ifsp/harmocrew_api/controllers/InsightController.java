package br.edu.ifsp.harmocrew_api.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifsp.harmocrew_api.dtos.InsightDtos.DashboardResponse;
import br.edu.ifsp.harmocrew_api.dtos.InsightDtos.TimelineResponse;
import br.edu.ifsp.harmocrew_api.services.InsightService;

@RestController
@RequestMapping("/api")
public class InsightController {

	private final InsightService insightService;

	public InsightController(InsightService insightService) {
		this.insightService = insightService;
	}

	// Timeline/Gantt por etapa do workflow musical.
	@GetMapping("/projects/{projectId}/timeline")
	public TimelineResponse timeline(@PathVariable Long projectId) {
		return insightService.timeline(projectId);
	}

	// Dashboard de um projeto.
	@GetMapping("/projects/{projectId}/dashboard")
	public DashboardResponse projectDashboard(@PathVariable Long projectId) {
		return insightService.projectDashboard(projectId);
	}

	// Dashboard do coletivo (todos os projetos) ou de um artista (?artistId=).
	@GetMapping("/dashboard")
	public DashboardResponse dashboard(@RequestParam(required = false) Long artistId) {
		return insightService.collectiveDashboard(artistId);
	}
}
