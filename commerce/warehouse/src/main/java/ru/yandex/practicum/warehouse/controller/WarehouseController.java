package ru.yandex.practicum.warehouse.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.interaction_api.dto.*;
import ru.yandex.practicum.interaction_api.feignApi.WarehouseApi;
import ru.yandex.practicum.warehouse.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WarehouseController implements WarehouseApi {
	private final WarehouseService warehouseService;

	@Override
	public void newProductInWarehouse(@Valid NewProductInWarehouseRequest request) {
		log.info("PUT /api/v1/warehouse - registering product: {}", request.getProductId());
		warehouseService.newProductInWarehouse(request);
	}

	@Override
	public void shippedToDelivery(@Valid ShippedToDeliveryRequest request) {
		log.info("POST /api/v1/warehouse/shipped - order {} linked to delivery {}",
				request.getOrderId(), request.getDeliveryId());
		warehouseService.shippedToDelivery(request);
	}

	@Override
	public void acceptReturn(@Valid Map<UUID, Long> products) {
		log.info("POST /api/v1/warehouse/return - accepting return for {} products", products.size());
		warehouseService.acceptReturn(products);
	}

	@Override
	public BookedProductsDto checkProductQuantityEnoughForShoppingCart(@Valid ShoppingCartDto shoppingCart) {
		log.info("POST /api/v1/warehouse/check - checking cart: {}", shoppingCart.getShoppingCartId());
		return warehouseService.checkProductQuantityEnoughForShoppingCart(shoppingCart);
	}

	@Override
	public BookedProductsDto assemblyProductsForOrder(@Valid AssemblyProductsForOrderRequest request) {
		log.info("POST /api/v1/warehouse/assembly - assembling order: {}", request.getOrderId());
		return warehouseService.assemblyProductsForOrder(request);
	}

	@Override
	public void addProductToWarehouse(@Valid AddProductToWarehouseRequest request) {
		log.info("POST /api/v1/warehouse/add - adding product: {}, quantity: {}",
				request.getProductId(), request.getQuantity());
		warehouseService.addProductToWarehouse(request);
	}

	@Override
	public AddressDto getWarehouseAddress() {
		log.info("GET /api/v1/warehouse/address");
		return warehouseService.getWarehouseAddress();
	}
}