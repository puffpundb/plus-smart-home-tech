package ru.yandex.practicum.shopping_cart.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.shopping_cart.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.interaction_api.dto.ShoppingCartDto;
import ru.yandex.practicum.shopping_cart.service.ShoppingCartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shopping-cart")
public class ShoppingCartController {

	private final ShoppingCartService shoppingCartService;

	public ShoppingCartController(ShoppingCartService shoppingCartService) {
		this.shoppingCartService = shoppingCartService;
	}

	@GetMapping
	public ResponseEntity<ShoppingCartDto> getShoppingCart(
			@RequestParam String username
	) {
		ShoppingCartDto dto = shoppingCartService.getShoppingCart(username);
		return ResponseEntity.ok(dto);
	}

	@PutMapping
	public ResponseEntity<ShoppingCartDto> addProductToShoppingCart(
			@RequestParam String username,
			@RequestBody Map<UUID, Long> productsToAdd
	) {
		ShoppingCartDto dto = shoppingCartService.addProducts(username, productsToAdd);
		return ResponseEntity.ok(dto);
	}

	@DeleteMapping
	public ResponseEntity<Void> deactivateCurrentShoppingCart(
			@RequestParam String username
	) {
		shoppingCartService.deactivateCurrentShoppingCart(username);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/remove")
	public ResponseEntity<ShoppingCartDto> removeFromShoppingCart(
			@RequestParam String username,
			@RequestBody List<UUID> productsToRemove
	) {
		ShoppingCartDto dto = shoppingCartService.removeFromShoppingCart(username, productsToRemove);
		return ResponseEntity.ok(dto);
	}

	@PostMapping("/change-quantity")
	public ResponseEntity<ShoppingCartDto> changeProductQuantity(
			@RequestParam String username,
			@RequestBody ChangeProductQuantityRequest request
	) {
		ShoppingCartDto dto = shoppingCartService.changeProductQuantity(username, request);
		return ResponseEntity.ok(dto);
	}
}