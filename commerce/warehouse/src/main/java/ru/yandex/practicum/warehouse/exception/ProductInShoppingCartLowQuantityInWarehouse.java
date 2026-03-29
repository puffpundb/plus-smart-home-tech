package ru.yandex.practicum.warehouse.exception;

public class ProductInShoppingCartLowQuantityInWarehouse extends RuntimeException {
  public ProductInShoppingCartLowQuantityInWarehouse(String message) {
    super(message);
  }
}
