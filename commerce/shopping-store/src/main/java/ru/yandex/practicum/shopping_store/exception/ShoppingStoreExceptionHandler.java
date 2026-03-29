package ru.yandex.practicum.shopping_store.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ShoppingStoreExceptionHandler {

	@ExceptionHandler
	public ResponseEntity<ProductNotFoundException> handleProductNotFound(ProductNotFoundException ex) {
		return ResponseEntity
				.status(ex.getHttpStatus())
				.body(ex);
	}
}
