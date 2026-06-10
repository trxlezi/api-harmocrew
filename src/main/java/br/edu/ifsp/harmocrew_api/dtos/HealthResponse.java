package br.edu.ifsp.harmocrew_api.dtos;

import java.time.Instant;

public record HealthResponse(String status, String application, Instant timestamp) {
}
