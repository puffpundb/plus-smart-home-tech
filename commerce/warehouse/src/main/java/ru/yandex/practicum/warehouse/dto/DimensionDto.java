package ru.yandex.practicum.warehouse.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DimensionDto {
	@Min(value = 1, message = "width должен быть >= 1")
	private Double width;

	@Min(value = 1, message = "height должен быть >= 1")
	private Double height;

	@Min(value = 1, message = "depth должен быть >= 1")
	private Double depth;
}