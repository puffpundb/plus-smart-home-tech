package ru.yandex.practicum.warehouse.mapper;

import ru.yandex.practicum.interaction_api.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.interaction_api.dto.AddressDto;
import ru.yandex.practicum.interaction_api.dto.BookedProductsDto;
import ru.yandex.practicum.interaction_api.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.warehouse.entity.WarehouseProductEntity;

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

	public static WarehouseProductEntity toAddEntity(AddProductToWarehouseRequest request) {
		return WarehouseProductEntity.builder()
				.productId(request.getProductId())
				.quantity(request.getQuantity())
				.build();
	}

	public static BookedProductsDto toBookedProductsDto(WarehouseProductEntity entity, long quantity) {
		return BookedProductsDto.builder()
				.deliveryWeight(entity.getWeight() * quantity)
				.deliveryVolume(entity.getWidth() * entity.getHeight() * entity.getDepth() * quantity)
				.fragile(entity.getFragile())
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
}