package ru.yandex.practicum.warehouse.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.interaction_api.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.interaction_api.dto.AddressDto;
import ru.yandex.practicum.interaction_api.dto.BookedProductsDto;
import ru.yandex.practicum.interaction_api.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.interaction_api.dto.ShoppingCartDto;
import ru.yandex.practicum.interaction_api.warehouse.WarehouseApi;
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
}