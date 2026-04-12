package ru.yandex.practicum.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(NoOrderFoundException.class)
	public ResponseEntity<Map<String, Object>> handleNotFound(NoOrderFoundException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(Map.of(
						"httpStatus", HttpStatus.BAD_REQUEST.toString(),
						"message", ex.getMessage()
				));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(Map.of(
						"httpStatus", HttpStatus.BAD_REQUEST.toString(),
						"message", ex.getMessage()
				));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("httpStatus", HttpStatus.INTERNAL_SERVER_ERROR.toString(),
						"message", ex.getMessage()));
	}
}
