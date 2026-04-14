package ru.yandex.practicum.interaction_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.interaction_api.enums.DeliveryState;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDto {
	private UUID deliveryId;

	@NotNull(message = "Адрес склада (fromAddress) обязателен")
	@Valid
	private AddressDto fromAddress;

	@NotNull(message = "Адрес доставки (toAddress) обязателен")
	@Valid
	private AddressDto toAddress;

	@NotNull(message = "Идентификатор заказа (orderId) обязателен")
	private UUID orderId;

	private DeliveryState deliveryState;
}
