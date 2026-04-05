package ru.yandex.practicum.shopping_cart.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.interaction_api.dto.BookedProductsDto;
import ru.yandex.practicum.interaction_api.dto.ShoppingCartDto;
import ru.yandex.practicum.shopping_cart.client.WarehouseClient;
import ru.yandex.practicum.shopping_cart.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.shopping_cart.entity.CartItemEntity;
import ru.yandex.practicum.shopping_cart.entity.ShoppingCartEntity;
import ru.yandex.practicum.shopping_cart.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.shopping_cart.exception.NotAuthorizedUserException;
import ru.yandex.practicum.shopping_cart.repository.CartItemRepository;
import ru.yandex.practicum.shopping_cart.repository.ShoppingCartRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.yandex.practicum.shopping_cart.mapper.ShoppingCartMapper.toShoppingCartDto;

@Service
@RequiredArgsConstructor
public class ShoppingCartService {

	private final ShoppingCartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final WarehouseClient warehouseClient;

	private ShoppingCartEntity createShoppingCart(String username) {
		ShoppingCartEntity newCart = new ShoppingCartEntity();
		newCart.setUsername(username);
		newCart.setActive(true);
		return cartRepository.save(newCart);
	}

	@Transactional
	public ShoppingCartDto getShoppingCart(String username) {
		ShoppingCartEntity cart = cartRepository
				.findByUsernameAndIsActive(username, true)
				.orElseGet(() -> {
					return createShoppingCart(username);
				});

		List<CartItemEntity> items = cartItemRepository.findByCartId(cart.getId());
		cart.setItems(items);

		return toShoppingCartDto(cart);
	}

	@Transactional
	public ShoppingCartDto addProducts(String username, Map<UUID, Long> productsToAdd) {
		ShoppingCartEntity cart = cartRepository.findByUsernameAndIsActive(username, true)
				.orElseGet(() -> createShoppingCart(username));

		List<CartItemEntity> items = cartItemRepository.findByCartId(cart.getId());
		Map<UUID, CartItemEntity> byProductId = items.stream()
				.collect(Collectors.toMap(CartItemEntity::getProductId, i -> i));

		List<CartItemEntity> newItems = new ArrayList<>();

		for (Map.Entry<UUID, Long> entry : productsToAdd.entrySet()) {
			UUID productId = entry.getKey();
			long delta = entry.getValue();

			CartItemEntity item = byProductId.get(productId);
			if (item == null) {
				item = new CartItemEntity();
				item.setCart(cart);
				item.setProductId(productId);
				item.setQuantity(delta);
				newItems.add(item);
				byProductId.put(productId, item);
			} else {
				item.setQuantity(item.getQuantity() + delta);
			}
		}

		if (!newItems.isEmpty()) {
			cartItemRepository.saveAll(newItems);
		}

		items.addAll(newItems);
		cart.setItems(items);

		ShoppingCartDto shoppingCartDto = toShoppingCartDto(cart);
		BookedProductsDto booked = warehouseClient.checkProductQuantityEnoughForShoppingCart(shoppingCartDto);

		return shoppingCartDto;
	}

	@Transactional
	public void deactivateCurrentShoppingCart(String username) {
		ShoppingCartEntity cart = cartRepository
				.findByUsernameAndIsActive(username, true)
				.orElseThrow(() ->
						new NotAuthorizedUserException("Актуальная корзина для пользователя не найдена"));

		cart.setActive(false);
	}

	@Transactional
	public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productsToRemove) {
		ShoppingCartEntity cart = cartRepository
				.findByUsernameAndIsActive(username, true)
				.orElseThrow(() ->
						new NotAuthorizedUserException("Актуальная корзина для пользователя не найдена"));

		List<CartItemEntity> existing =
				cartItemRepository.findByCartIdAndProductIdIn(cart.getId(), productsToRemove);

		if (existing.isEmpty()) {
			throw new NoProductsInShoppingCartException("Нет искомых товаров в корзине");
		}

		cartItemRepository.deleteByCartIdAndProductIdIn(cart.getId(), productsToRemove);

		List<CartItemEntity> items = cartItemRepository.findByCartId(cart.getId());
		cart.setItems(items);

		return toShoppingCartDto(cart);
	}

	@Transactional
	public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
		ShoppingCartEntity cart = cartRepository.findByUsernameAndIsActive(username, true)
				.orElseThrow(() -> new NotAuthorizedUserException("Актуальная корзина для пользователя не найдена"));

		CartItemEntity item = cartItemRepository
				.findByCartIdAndProductIdIn(cart.getId(), List.of(request.getProductId()))
				.stream()
				.findFirst()
				.orElseThrow(() -> new NoProductsInShoppingCartException("Нет искомых товаров в корзине"));

		item.setQuantity(request.getNewQuantity());
		cartItemRepository.save(item);

		List<CartItemEntity> items = cartItemRepository.findByCartId(cart.getId());
		cart.setItems(items);

		ShoppingCartDto shoppingCartDto = toShoppingCartDto(cart);
		warehouseClient.checkProductQuantityEnoughForShoppingCart(shoppingCartDto);

		return shoppingCartDto;
	}
}