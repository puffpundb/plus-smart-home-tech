package ru.yandex.practicum.warehouse.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.interaction_api.dto.*;
import ru.yandex.practicum.interaction_api.feignApi.WarehouseApi;
import ru.yandex.practicum.warehouse.service.WarehouseService;

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
	public BookedProductsDto checkProductQuantityEnoughForShoppingCart(@Valid ShoppingCartDto cart) {
		log.info("POST /api/v1/warehouse/check - checking cart: {}", cart.getShoppingCartId());
		return warehouseService.checkProductQuantityEnoughForShoppingCart(cart);
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

	@Override
	public void assemblyProductForOrderFromShoppingCart(@Valid ShoppingCartDto cart) {
		log.info("POST /api/v1/warehouse/assembly - assembling order from cart: {}", cart.getShoppingCartId());
		warehouseService.assemblyProductForOrderFromShoppingCart(cart);
	}

	@Override
	public void shippedToDelivery(@Valid ShippedToDeliveryRequest request) {
		log.info("POST /api/v1/warehouse/shipped - order {} linked to delivery {}",
				request.getOrderId(), request.getDeliveryId());
		warehouseService.shippedToDelivery(request);
	}

	@Override
	public void returnProducts(@Valid ProductReturnRequest request) {
		log.info("POST /api/v1/warehouse/return - returning products for order: {}", request.getOrderId());
		warehouseService.returnProducts(request);
	}
}