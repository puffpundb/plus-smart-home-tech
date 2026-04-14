package ru.yandex.practicum.shopping_store.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.shopping_store.entity.enums.QuantityState;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetProductQuantityStateRequest {
	@NotNull(message = "productId не может быть null")
	private UUID productId;

	@NotNull(message = "quantityState не может быть null")
	private QuantityState quantityState;
}
