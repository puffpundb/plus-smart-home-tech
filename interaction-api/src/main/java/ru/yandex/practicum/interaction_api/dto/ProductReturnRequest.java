package ru.yandex.practicum.interaction_api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReturnRequest {
	@NotNull(message = "Идентификатор заказа (orderId) обязателен для возврата")
	private UUID orderId;

	@NotNull(message = "Список товаров для возврата (products) обязателен")
	private Map<UUID, Long> products;
}
