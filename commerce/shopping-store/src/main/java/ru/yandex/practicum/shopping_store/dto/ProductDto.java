package ru.yandex.practicum.shopping_store.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.shopping_store.entity.enums.ProductCategory;
import ru.yandex.practicum.shopping_store.entity.enums.ProductState;
import ru.yandex.practicum.shopping_store.entity.enums.QuantityState;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
	private UUID productId;

	@NotBlank(message = "productName не может быть пустым")
	private String productName;

	@NotBlank(message = "description не может быть пустым")
	private String description;

	private String imageSrc;

	@NotNull(message = "quantityState не может быть null")
	private QuantityState quantityState;

	@NotNull(message = "productState не может быть null")
	private ProductState productState;

	@NotNull(message = "productCategory не может быть null")
	private ProductCategory productCategory;

	@NotNull(message = "price не может быть null")
	@DecimalMin(value = "1.0", message = "price должен быть > 0.0")
	private BigDecimal price;
}
