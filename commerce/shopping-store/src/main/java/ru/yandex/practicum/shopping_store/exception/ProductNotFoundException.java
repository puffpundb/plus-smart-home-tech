package ru.yandex.practicum.shopping_store.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductNotFoundException extends RuntimeException {
	private final HttpStatus httpStatus;
	private final String userMessage;

	public ProductNotFoundException(String message) {
		super(message);
		this.httpStatus = HttpStatus.NOT_FOUND;
		this.userMessage = message;
	}
}
