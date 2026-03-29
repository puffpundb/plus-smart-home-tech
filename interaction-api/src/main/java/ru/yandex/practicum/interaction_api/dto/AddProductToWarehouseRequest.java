package ru.yandex.practicum.interaction_api.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddProductToWarehouseRequest {
	private UUID productId;

	@Min(value = 1, message = "quantity должен быть >= 1")
	private Long quantity;
}