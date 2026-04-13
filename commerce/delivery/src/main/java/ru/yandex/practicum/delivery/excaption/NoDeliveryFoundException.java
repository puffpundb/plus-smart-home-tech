package ru.yandex.practicum.delivery.excaption;

public class NoDeliveryFoundException extends RuntimeException {
	public NoDeliveryFoundException(String message) {
		super(message);
	}
}
