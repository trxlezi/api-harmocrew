package br.edu.ifsp.harmocrew_api.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.edu.ifsp.harmocrew_api.dtos.AuditLogResponse;
import br.edu.ifsp.harmocrew_api.dtos.FileDtos.FileVersionResponse;
import br.edu.ifsp.harmocrew_api.dtos.FileDtos.ProjectFileResponse;
import br.edu.ifsp.harmocrew_api.dtos.FileDtos.UnlockRequest;
import br.edu.ifsp.harmocrew_api.enums.FileCategory;
import br.edu.ifsp.harmocrew_api.services.AuditLogService;
import br.edu.ifsp.harmocrew_api.services.ProjectFileService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class ProjectFileController {

	private final ProjectFileService fileService;
	private final AuditLogService auditLogService;

	public ProjectFileController(ProjectFileService fileService, AuditLogService auditLogService) {
		this.fileService = fileService;
		this.auditLogService = auditLogService;
	}

	@GetMapping("/projects/{projectId}/files")
	public List<ProjectFileResponse> list(@PathVariable Long projectId, @RequestParam(required = false) FileCategory category) {
		return fileService.list(projectId, category);
	}

	// Multipart: file (obrigatorio), name, category, description, note, artistId.
	@PostMapping(path = "/projects/{projectId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public ProjectFileResponse create(
		@PathVariable Long projectId,
		@RequestParam("file") MultipartFile file,
		@RequestParam(required = false) String name,
		@RequestParam(required = false) FileCategory category,
		@RequestParam(required = false) String description,
		@RequestParam(required = false) String note,
		@RequestParam(required = false) Long artistId
	) {
		return fileService.create(projectId, file, name, category, description, note, artistId);
	}

	@GetMapping("/files/{id}")
	public ProjectFileResponse get(@PathVariable Long id) {
		return fileService.get(id);
	}

	@PostMapping(path = "/files/{id}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public ProjectFileResponse uploadVersion(
		@PathVariable Long id,
		@RequestParam("file") MultipartFile file,
		@RequestParam(required = false) String note,
		@RequestParam(required = false) Long artistId
	) {
		return fileService.uploadVersion(id, file, note, artistId);
	}

	// Metadados de uma versao especifica (inclui o link imutavel "url").
	@GetMapping("/files/{id}/versions/{versionNumber}")
	public FileVersionResponse version(@PathVariable Long id, @PathVariable int versionNumber) {
		return fileService.getVersion(id, versionNumber);
	}

	@PostMapping("/files/{id}/unlock")
	public ProjectFileResponse unlock(@PathVariable Long id, @Valid @RequestBody UnlockRequest request) {
		return fileService.unlock(id, request.artistId(), request.reason());
	}

	@GetMapping("/files/{id}/history")
	public List<AuditLogResponse> history(@PathVariable Long id) {
		return auditLogService.listByEntity("ProjectFile", id);
	}

	// Log de auditoria completo do projeto (somente leitura).
	@GetMapping("/projects/{projectId}/audit-logs")
	public List<AuditLogResponse> auditLogs(@PathVariable Long projectId) {
		return auditLogService.listByProject(projectId);
	}
}
