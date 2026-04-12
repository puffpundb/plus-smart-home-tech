package ru.yandex.practicum.interaction_api.dto;

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
	private UUID orderId;
	private UUID shoppingCartId;
	private Map<UUID, Long> products;
	private UUID paymentId;
	private UUID deliveryId;
	private OrderStatus state;
	private Double deliveryWeight;
	private Double deliveryVolume;
	private Boolean fragile;
	private Double totalPrice;
	private Double deliveryPrice;
	private Double productPrice;
}
