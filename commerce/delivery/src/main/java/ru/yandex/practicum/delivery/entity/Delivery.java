package ru.yandex.practicum.delivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.interaction_api.enums.DeliveryState;

import java.util.UUID;

@Entity
@Table(name = "deliveries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {
	@Id
	@Column(name = "delivery_id")
	private UUID deliveryId;

	@Column(name = "order_id", nullable = false, unique = true)
	private UUID orderId;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private DeliveryState status;

	@Column(name = "from_country")
	private String fromCountry;
	@Column(name = "from_city")
	private String fromCity;
	@Column(name = "from_street")
	private String fromStreet;
	@Column(name = "from_house")
	private String fromHouse;
	@Column(name = "from_flat")
	private String fromFlat;

	@Column(name = "to_country")
	private String toCountry;
	@Column(name = "to_city")
	private String toCity;
	@Column(name = "to_street")
	private String toStreet;
	@Column(name = "to_house")
	private String toHouse;
	@Column(name = "to_flat")
	private String toFlat;

	@Column(name = "weight")
	private Double weight;
	@Column(name = "volume")
	private Double volume;
	@Column(name = "fragile")
	private Boolean fragile;

}
