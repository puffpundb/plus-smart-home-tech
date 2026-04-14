package ru.yandex.practicum.interaction_api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.interaction_api.enums.OrderStatus;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
	@NotNull(message = "Идентификатор заказа (orderId) обязателен")
	private UUID orderId;

	private UUID shoppingCartId;

	@NotNull(message = "Список товаров (products) обязателен")
	private Map<UUID, Long> products;

	private UUID paymentId;

	private UUID deliveryId;

	private OrderStatus state;

	@NotNull(message = "Вес доставки (deliveryWeight) обязателен для расчёта стоимости")
	private Double deliveryWeight;

	@NotNull(message = "Объём доставки (deliveryVolume) обязателен для расчёта стоимости")
	private Double deliveryVolume;

	@NotNull(message = "Признак хрупкости (fragile) обязателен для расчёта стоимости")
	private Boolean fragile;

	private Double totalPrice;

	private Double deliveryPrice;

	private Double productPrice;
}
