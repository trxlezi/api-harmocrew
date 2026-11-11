package br.edu.ifsp.harmocrew_api.dtos;

import java.time.LocalDateTime;
import java.util.List;

import br.edu.ifsp.harmocrew_api.enums.FileCategory;
import jakarta.validation.constraints.Size;

// DTOs do versionamento de arquivos (Funcionalidade 2).
public final class FileDtos {

	private FileDtos() {
	}

	public record FileVersionResponse(
		Long id,
		Long fileId,
		int versionNumber,
		String url,
		String originalFilename,
		String contentType,
		long sizeBytes,
		String sha256,
		String note,
		Long uploadedByArtistId,
		String uploadedByName,
		LocalDateTime uploadedAt,
		boolean decided
	) {
	}

	public record ProjectFileResponse(
		Long id,
		Long projectId,
		String name,
		FileCategory category,
		String description,
		LocalDateTime createdAt,
		Long createdByArtistId,
		String createdByName,
		boolean locked,
		Long lockedVersionId,
		Integer lockedVersionNumber,
		Long lockedByPollId,
		LocalDateTime lockedAt,
		FileVersionResponse currentVersion,
		List<FileVersionResponse> versions
	) {
	}

	public record UnlockRequest(
		Long artistId,
		@Size(max = 1000)
		String reason
	) {
	}
}
