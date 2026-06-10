package br.edu.ifsp.harmocrew_api.dtos;

import br.edu.ifsp.harmocrew_api.enums.DecisionStatus;
import jakarta.validation.constraints.NotNull;

public record DecisionStatusUpdateRequest(
	@NotNull
	DecisionStatus status
) {
}
