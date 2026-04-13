package ru.yandex.practicum.delivery.excaption;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(NoDeliveryFoundException.class)
	public ResponseEntity<Map<String, Object>> handleNotFound(NoDeliveryFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(Map.of(
						"httpStatus", HttpStatus.NOT_FOUND.toString(),
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
