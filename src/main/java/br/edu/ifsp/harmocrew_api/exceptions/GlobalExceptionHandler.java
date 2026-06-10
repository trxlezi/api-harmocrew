package br.edu.ifsp.harmocrew_api.exceptions;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(
		MethodArgumentNotValidException exception,
		HttpServletRequest request
	) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors().forEach(error ->
			fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));

		HttpStatus status = HttpStatus.BAD_REQUEST;
		ErrorResponse response = ErrorResponse.withFieldErrors(
			status.value(),
			status.getReasonPhrase(),
			"Erro de validacao",
			request.getRequestURI(),
			fieldErrors);

		return ResponseEntity.status(status).body(response);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFound(
		ResourceNotFoundException exception,
		HttpServletRequest request
	) {
		return build(HttpStatus.NOT_FOUND, exception.getMessage(), request);
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusiness(BusinessException exception, HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
	}

	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(
		EmailAlreadyExistsException exception,
		HttpServletRequest request
	) {
		return build(HttpStatus.CONFLICT, exception.getMessage(), request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneric(Exception exception, HttpServletRequest request) {
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado", request);
	}

	private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
		ErrorResponse response = ErrorResponse.withoutFieldErrors(
			status.value(),
			status.getReasonPhrase(),
			message,
			request.getRequestURI());

		return ResponseEntity.status(status).body(response);
	}
}
