package ru.yandex.practicum.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.interaction_api.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
	@Id
	@Column(name = "order_id")
	private UUID orderId;

	@Column(name = "username", nullable = false)
	private String username;

	@Column(name = "shopping_cart_id")
	private UUID shoppingCartId;

	@Column(name = "payment_id")
	private UUID paymentId;

	@Column(name = "delivery_id")
	private UUID deliveryId;

	@Enumerated(EnumType.STRING)
	@Column(name = "state", nullable = false)
	private OrderStatus state;

	@Column(name = "delivery_weight", nullable = false)
	private Double deliveryWeight;

	@Column(name = "delivery_volume", nullable = false)
	private Double deliveryVolume;

	@Column(name = "fragile", nullable = false)
	private Boolean fragile;

	@Column(name = "total_price")
	private Double totalPrice;

	@Column(name = "delivery_price")
	private Double deliveryPrice;

	@Column(name = "product_price")
	private Double productPrice;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Builder.Default
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<OrderItem> items = new ArrayList<>();

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
		if (orderId == null) {
			orderId = UUID.randomUUID();
		}
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public void addItem(OrderItem item) {
		items.add(item);
		item.setOrder(this);
	}

	public void removeItem(OrderItem item) {
		items.remove(item);
		item.setOrder(null);
	}
}
