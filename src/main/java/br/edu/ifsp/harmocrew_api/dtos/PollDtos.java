package br.edu.ifsp.harmocrew_api.dtos;

import java.time.LocalDateTime;
import java.util.List;

import br.edu.ifsp.harmocrew_api.enums.PollStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// DTOs das enquetes de decisao em grupo (Funcionalidade 2).
public final class PollDtos {

	private PollDtos() {
	}

	public record PollOptionRequest(
		@NotBlank
		@Size(max = 200)
		String label,
		Long fileVersionId
	) {
	}

	public record PollRequest(
		@NotBlank
		@Size(max = 200)
		String question,
		@Size(max = 2000)
		String description,
		LocalDateTime closesAt,
		Long createdByArtistId,
		@NotNull
		@Size(min = 2, max = 20)
		List<@Valid PollOptionRequest> options
	) {
	}

	public record VoteRequest(
		Long artistId,
		@NotNull
		Long optionId
	) {
	}

	public record ClosePollRequest(
		Long artistId,
		Long tieBreakOptionId
	) {
	}

	public record PollOptionResponse(
		Long id,
		String label,
		int position,
		Long fileVersionId,
		Long fileId,
		Integer fileVersionNumber,
		String fileVersionUrl,
		long votes,
		List<String> voters
	) {
	}

	public record PollResponse(
		Long id,
		Long projectId,
		String question,
		String description,
		PollStatus status,
		Long createdByArtistId,
		String createdByName,
		LocalDateTime createdAt,
		LocalDateTime closesAt,
		LocalDateTime closedAt,
		Long winningOptionId,
		Long decisionRecordId,
		long totalVotes,
		List<PollOptionResponse> options
	) {
	}
}
