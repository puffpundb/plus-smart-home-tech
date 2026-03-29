package ru.yandex.practicum.shopping_cart.exception;

import org.springframework.http.HttpStatus;

public class NotAuthorizedUserException extends RuntimeException {

  private final HttpStatus httpStatus;
  private final String userMessage;

  public NotAuthorizedUserException(String message) {
    super(message);
    this.httpStatus = HttpStatus.UNAUTHORIZED;
    this.userMessage = message;
  }

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }

  public String getUserMessage() {
    return userMessage;
  }
}