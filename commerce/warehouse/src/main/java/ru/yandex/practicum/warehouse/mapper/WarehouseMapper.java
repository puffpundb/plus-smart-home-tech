package ru.yandex.practicum.warehouse.mapper;

import ru.yandex.practicum.interaction_api.dto.AddressDto;
import ru.yandex.practicum.interaction_api.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.warehouse.entity.OrderBooking;
import ru.yandex.practicum.warehouse.entity.OrderBookingItem;
import ru.yandex.practicum.warehouse.entity.WarehouseProductEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WarehouseMapper {

	public static WarehouseProductEntity toEntity(NewProductInWarehouseRequest request) {
		return WarehouseProductEntity.builder()
				.productId(request.getProductId())
				.fragile(request.getFragile() != null ? request.getFragile() : false)
				.width(request.getDimension().getWidth())
				.height(request.getDimension().getHeight())
				.depth(request.getDimension().getDepth())
				.weight(request.getWeight())
				.quantity(0L)
				.build();
	}

	public static AddressDto toAddressDto(String address) {
		return AddressDto.builder()
				.country(address)
				.city(address)
				.street(address)
				.house(address)
				.flat(address)
				.build();
	}

	public static OrderBooking toBookingEntity(UUID orderId,
											   Map<UUID, Long> products,
											   List<WarehouseProductEntity> warehouseProducts,
											   Double totalWeight,
											   Double totalVolume,
											   Boolean hasFragile) {

		OrderBooking booking = OrderBooking.builder()
				.orderId(orderId)
				.totalWeight(totalWeight)
				.totalVolume(totalVolume)
				.hasFragile(hasFragile)
				.items(new ArrayList<>())
				.build();

		for (Map.Entry<UUID, Long> entry : products.entrySet()) {
			UUID productId = entry.getKey();
			Long quantity = entry.getValue();

			OrderBookingItem item = OrderBookingItem.builder()
					.booking(booking)
					.productId(productId)
					.quantity(quantity)
					.build();

			booking.getItems().add(item);
		}

		return booking;
	}
}