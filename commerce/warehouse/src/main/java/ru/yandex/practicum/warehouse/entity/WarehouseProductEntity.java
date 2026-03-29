package ru.yandex.practicum.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "warehouse")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseProductEntity {
	@Id
	@Column(name = "product_id", nullable = false)
	private UUID productId;

	@Column(name = "fragile", nullable = false)
	@Builder.Default
	private Boolean fragile = false;

	@Column(name = "width", nullable = false)
	private Double width;

	@Column(name = "height", nullable = false)
	private Double height;

	@Column(name = "depth", nullable = false)
	private Double depth;

	@Column(name = "weight", nullable = false)
	private Double weight;

	@Column(name = "quantity", nullable = false)
	@Builder.Default
	private Long quantity = 0L;
}