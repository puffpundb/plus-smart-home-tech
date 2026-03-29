package ru.yandex.practicum.warehouse.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.warehouse.dto.*;
import ru.yandex.practicum.warehouse.service.WarehouseService;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

	private final WarehouseService warehouseService;

	@PutMapping
	public ResponseEntity<Void> newProductInWarehouse(
			@Valid @RequestBody NewProductInWarehouseRequest request
	) {
		log.info("PUT /api/v1/warehouse - registering product: {}", request.getProductId());
		warehouseService.newProductInWarehouse(request);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/check")
	public ResponseEntity<BookedProductsDto> checkProductQuantityEnoughForShoppingCart(
			@Valid @RequestBody ShoppingCartDto cart
	) {
		log.info("POST /api/v1/warehouse/check - checking cart: {}", cart.getShoppingCartId());
		BookedProductsDto result = warehouseService.checkProductQuantityEnoughForShoppingCart(cart);
		return ResponseEntity.ok(result);
	}

	@PostMapping("/add")
	public ResponseEntity<Void> addProductToWarehouse(
			@Valid @RequestBody AddProductToWarehouseRequest request
	) {
		log.info("POST /api/v1/warehouse/add - adding product: {}, quantity: {}",
				request.getProductId(), request.getQuantity());
		warehouseService.addProductToWarehouse(request);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/address")
	public ResponseEntity<AddressDto> getWarehouseAddress() {
		log.info("GET /api/v1/warehouse/address");
		AddressDto address = warehouseService.getWarehouseAddress();
		return ResponseEntity.ok(address);
	}
}