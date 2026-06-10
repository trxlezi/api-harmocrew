package br.edu.ifsp.harmocrew_api.controllers;

import java.time.Instant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifsp.harmocrew_api.dtos.HealthResponse;

@RestController
public class HealthController {

	@GetMapping("/health")
	public HealthResponse health() {
		return new HealthResponse("UP", "harmocrew-api", Instant.now());
	}
}
