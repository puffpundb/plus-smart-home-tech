package ru.yandex.practicum.warehouse.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "order_bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderBooking {
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "booking_id")
	private UUID bookingId;

	@Column(name = "order_id", nullable = false, unique = true)
	private UUID orderId;

	@Column(name = "delivery_id")
	private UUID deliveryId;

	@Column(name = "total_weight")
	private Double totalWeight;

	@Column(name = "total_volume")
	private Double totalVolume;

	@Column(name = "has_fragile")
	private Boolean hasFragile;

	@OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<OrderBookingItem> items = new ArrayList<>();
}
