package ru.yandex.practicum.warehouse.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewProductInWarehouseRequest {
	@NotNull(message = "productId не может быть null")
	private UUID productId;

	private Boolean fragile;

	@NotNull(message = "dimension не может быть null")
	@Valid
	private DimensionDto dimension;

	@NotNull(message = "weight не может быть null")
	@Min(value = 1, message = "weight должен быть >= 1")
	private Double weight;
}