package ru.yandex.practicum.shopping_cart.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "shopping_cart")
@Data
public class ShoppingCartEntity {
	@Id
	@GeneratedValue
	@Column(columnDefinition = "UUID DEFAULT gen_random_uuid()")
	private UUID id;

	@Column(name = "username", nullable = false)
	private String username;

	@Column(name = "is_active", nullable = false)
	private boolean isActive = true;

	@OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<CartItemEntity> items;
}