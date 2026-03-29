package ru.yandex.practicum.warehouse.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(SpecifiedProductAlreadyInWarehouseException.class)
	public ResponseEntity<SpecifiedProductAlreadyInWarehouseException> handleProductAlreadyExists(
			SpecifiedProductAlreadyInWarehouseException ex) {
		return ResponseEntity.badRequest().body(ex);
	}

	@ExceptionHandler(ProductInShoppingCartLowQuantityInWarehouse.class)
	public ResponseEntity<ProductInShoppingCartLowQuantityInWarehouse> handleInsufficientQuantity(
			ProductInShoppingCartLowQuantityInWarehouse ex) {
		return ResponseEntity.badRequest().body(ex);
	}

	@ExceptionHandler(NoSpecifiedProductInWarehouseException.class)
	public ResponseEntity<NoSpecifiedProductInWarehouseException> handleProductNotFound(
			NoSpecifiedProductInWarehouseException ex) {
		return ResponseEntity.badRequest().body(ex);
	}
}