package ru.yandex.practicum.delivery.mapper;

import ru.yandex.practicum.delivery.entity.Delivery;
import ru.yandex.practicum.interaction_api.dto.AddressDto;
import ru.yandex.practicum.interaction_api.dto.DeliveryDto;
import ru.yandex.practicum.interaction_api.enums.DeliveryState;

import java.util.UUID;

public class DeliveryMapper {
	public static DeliveryDto toDto(Delivery delivery) {
		if (delivery == null) return null;

		AddressDto from = AddressDto.builder()
				.country(delivery.getFromCountry())
				.city(delivery.getFromCity())
				.street(delivery.getFromStreet())
				.house(delivery.getFromHouse())
				.flat(delivery.getFromFlat())
				.build();

		AddressDto to = AddressDto.builder()
				.country(delivery.getToCountry())
				.city(delivery.getToCity())
				.street(delivery.getToStreet())
				.house(delivery.getToHouse())
				.flat(delivery.getToFlat())
				.build();

		return DeliveryDto.builder()
				.deliveryId(delivery.getDeliveryId())
				.orderId(delivery.getOrderId())
				.fromAddress(from)
				.toAddress(to)
				.deliveryState(delivery.getStatus())
				.build();
	}

	public static Delivery toEntity(DeliveryDto dto) {
		if (dto == null) return null;

		Delivery.DeliveryBuilder builder = Delivery.builder()
				.orderId(dto.getOrderId());

		DeliveryState status = dto.getDeliveryState();
		if (status == null) {
			status = DeliveryState.CREATED;
		}
		builder.status(status);

		AddressDto from = dto.getFromAddress();
		if (from != null) {
			builder.fromCountry(from.getCountry())
					.fromCity(from.getCity())
					.fromStreet(from.getStreet())
					.fromHouse(from.getHouse())
					.fromFlat(from.getFlat());
		}

		AddressDto to = dto.getToAddress();
		if (to != null) {
			builder.toCountry(to.getCountry())
					.toCity(to.getCity())
					.toStreet(to.getStreet())
					.toHouse(to.getHouse())
					.toFlat(to.getFlat());
		}

		return builder.build();
	}
}
