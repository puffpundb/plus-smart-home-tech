package ru.yandex.practicum.warehouse.exception;

public class ProductInShoppingCartNotInWarehouse extends RuntimeException {
	public ProductInShoppingCartNotInWarehouse(String message) {
		super(message);
	}
}
