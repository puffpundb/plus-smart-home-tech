package ru.yandex.practicum.shopping_cart.mapper;

import ru.yandex.practicum.interaction_api.dto.ShoppingCartDto;
import ru.yandex.practicum.shopping_cart.entity.CartItemEntity;
import ru.yandex.practicum.shopping_cart.entity.ShoppingCartEntity;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ShoppingCartMapper {

	public static ShoppingCartDto toShoppingCartDto(ShoppingCartEntity cart) {
		Map<UUID, Long> products = cart.getItems().stream()
				.collect(Collectors.toMap(
						CartItemEntity::getProductId,
						CartItemEntity::getQuantity
				));

		ShoppingCartDto dto = new ShoppingCartDto();
		dto.setShoppingCartId(cart.getId());
		dto.setProducts(products);
		return dto;
	}
}