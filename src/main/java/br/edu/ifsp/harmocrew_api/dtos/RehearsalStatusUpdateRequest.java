package br.edu.ifsp.harmocrew_api.dtos;

import br.edu.ifsp.harmocrew_api.enums.RehearsalStatus;
import jakarta.validation.constraints.NotNull;

public record RehearsalStatusUpdateRequest(
	@NotNull
	RehearsalStatus status
) {
}
