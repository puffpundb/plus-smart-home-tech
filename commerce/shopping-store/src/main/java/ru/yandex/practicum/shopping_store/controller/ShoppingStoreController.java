package ru.yandex.practicum.shopping_store.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.shopping_store.dto.PageProductDto;
import ru.yandex.practicum.shopping_store.dto.ProductDto;
import ru.yandex.practicum.shopping_store.entity.enums.ProductCategory;
import ru.yandex.practicum.shopping_store.entity.enums.QuantityState;
import ru.yandex.practicum.shopping_store.service.ShoppingStoreService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/shopping-store")
@RequiredArgsConstructor
@Validated
public class ShoppingStoreController {
	private final ShoppingStoreService shoppingStoreService;

	@GetMapping
	public ResponseEntity<PageProductDto> getProducts(@RequestParam ProductCategory category,
													  @RequestParam(required = false, defaultValue = "0") @Min(0) int page,
													  @RequestParam(required = false, defaultValue = "20") @Min(1) int size,
													  @RequestParam(required = false) String sort) {
		log.info("GET /api/v1/shopping-store?category={}", category);

		return ResponseEntity.ok(shoppingStoreService.getProductsByCategory(category, page, size, sort));
	}

	@GetMapping("/{productId}")
	public ResponseEntity<ProductDto> getProduct(@PathVariable UUID productId) {
		log.info("GET /api/v1/shopping-store/{}", productId);

		return ResponseEntity.ok(shoppingStoreService.getProductById(productId));
	}

	@PutMapping
	public ResponseEntity<ProductDto> createNewProduct(@Valid @RequestBody ProductDto dto) {
		log.info("PUT /api/v1/shopping-store - creating: {}", dto.getProductName());

		return ResponseEntity.ok(shoppingStoreService.createProduct(dto));
	}

	@PostMapping
	public ResponseEntity<ProductDto> updateProduct(@Valid @RequestBody ProductDto dto) {
		log.info("POST /api/v1/shopping-store - updating: {}", dto.getProductId());

		return ResponseEntity.ok(shoppingStoreService.updateProduct(dto));
	}

	@PostMapping("/removeProductFromStore")
	public ResponseEntity<Boolean> removeProductFromStore(@RequestBody UUID productId) {
		log.info("POST /api/v1/shopping-store/removeProductFromStore - removing: {}", productId);

		return ResponseEntity.ok(shoppingStoreService.removeProductFromStore(productId));
	}

	@PostMapping("/quantityState")
	public ResponseEntity<Boolean> setProductQuantityState(@RequestParam UUID productId,
														   @RequestParam QuantityState quantityState) {
		log.info("POST /api/v1/shopping-store/quantityState - product: {}, state: {}", productId, quantityState);

		return ResponseEntity.ok(shoppingStoreService.setProductQuantityState(productId, quantityState));
	}
}