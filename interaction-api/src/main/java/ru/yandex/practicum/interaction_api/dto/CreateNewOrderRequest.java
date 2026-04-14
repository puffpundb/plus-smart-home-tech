package ru.yandex.practicum.interaction_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNewOrderRequest {
	@NotNull(message = "Корзина (shoppingCart) обязательна для создания заказа")
	@Valid
	private ShoppingCartDto shoppingCart;

	@NotNull(message = "Адрес доставки (deliveryAddress) обязателен для создания заказа")
	@Valid
	private AddressDto deliveryAddress;
}
