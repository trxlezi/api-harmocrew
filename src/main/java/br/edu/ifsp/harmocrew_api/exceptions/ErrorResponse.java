package br.edu.ifsp.harmocrew_api.exceptions;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
	LocalDateTime timestamp,
	int status,
	String error,
	String message,
	String path,
	Map<String, String> fieldErrors
) {
	public static ErrorResponse withoutFieldErrors(int status, String error, String message, String path) {
		return new ErrorResponse(LocalDateTime.now(), status, error, message, path, null);
	}

	public static ErrorResponse withFieldErrors(
		int status,
		String error,
		String message,
		String path,
		Map<String, String> fieldErrors
	) {
		return new ErrorResponse(LocalDateTime.now(), status, error, message, path, fieldErrors);
	}
}
