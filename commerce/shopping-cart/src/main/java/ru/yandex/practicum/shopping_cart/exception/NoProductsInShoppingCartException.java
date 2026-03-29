package ru.yandex.practicum.shopping_cart.exception;

import org.springframework.http.HttpStatus;

public class NoProductsInShoppingCartException extends RuntimeException {

  private final HttpStatus httpStatus;
  private final String userMessage;

  public NoProductsInShoppingCartException(String message) {
    super(message);
    this.httpStatus = HttpStatus.BAD_REQUEST;
    this.userMessage = message;
  }

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }

  public String getUserMessage() {
    return userMessage;
  }
}