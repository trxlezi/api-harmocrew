package br.edu.ifsp.harmocrew_api.dtos;

import br.edu.ifsp.harmocrew_api.enums.WeeklyGoalStatus;
import jakarta.validation.constraints.NotNull;

public record WeeklyGoalStatusUpdateRequest(
	@NotNull
	WeeklyGoalStatus status
) {
}
