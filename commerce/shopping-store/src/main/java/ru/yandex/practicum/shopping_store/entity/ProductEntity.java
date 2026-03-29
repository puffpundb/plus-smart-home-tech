package ru.yandex.practicum.shopping_store.entity;

import ru.yandex.practicum.shopping_store.entity.enums.ProductCategory;
import ru.yandex.practicum.shopping_store.entity.enums.ProductState;
import ru.yandex.practicum.shopping_store.entity.enums.QuantityState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@Table(name = "product")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", updatable = false)
	private UUID id;

	@Column(name = "product_name")
	private String productName;

	@Column(name = "description")
	private String description;

	@Column(name = "image_src")
	private String imageSrc;

	@Enumerated(EnumType.STRING)
	@Column(name = "quantity_state")
	private QuantityState quantityState;

	@Enumerated(EnumType.STRING)
	@Column(name = "product_state")
	private ProductState productState;

	@Enumerated(EnumType.STRING)
	@Column(name = "product_category")
	private ProductCategory productCategory;

	@Column(name = "price", precision = 10, scale = 2)
	private BigDecimal price;
}
