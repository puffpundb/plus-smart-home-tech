package ru.yandex.practicum.shopping_cart.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "cart_item")
@Data
public class CartItemEntity {
	@Id
	@GeneratedValue
	@Column(columnDefinition = "UUID DEFAULT gen_random_uuid()")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cart_id", nullable = false)
	private ShoppingCartEntity cart;

	@Column(name = "product_id", nullable = false)
	private UUID productId;

	@Column(name = "quantity", nullable = false)
	private Long quantity;
}