package ru.yandex.practicum.shopping_cart.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ShoppingCartExceptionHandler {

	@ExceptionHandler(NotAuthorizedUserException.class)
	public ResponseEntity<NotAuthorizedUserException> handleNotAuthorized(NotAuthorizedUserException ex) {
		return ResponseEntity.status(ex.getHttpStatus()).body(ex);
	}

	@ExceptionHandler(NoProductsInShoppingCartException.class)
	public ResponseEntity<NoProductsInShoppingCartException> handleNoProducts(NoProductsInShoppingCartException ex) {
		return ResponseEntity.status(ex.getHttpStatus()).body(ex);
	}
}