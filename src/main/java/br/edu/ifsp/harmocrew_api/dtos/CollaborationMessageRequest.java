package br.edu.ifsp.harmocrew_api.dtos;

import br.edu.ifsp.harmocrew_api.enums.CollaborationMessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CollaborationMessageRequest(
	@NotNull
	Long senderArtistId,

	@NotBlank
	@Size(max = 3000)
	String content,

	@NotNull
	CollaborationMessageType type
) {
}
