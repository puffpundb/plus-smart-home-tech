package ru.yandex.practicum.interaction_api.warehouse;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interaction_api.dto.*;

public interface WarehouseApi {

	@PutMapping("/api/v1/warehouse")
	void newProductInWarehouse(@RequestBody NewProductInWarehouseRequest request);

	@PostMapping("/api/v1/warehouse/check")
	BookedProductsDto checkProductQuantityEnoughForShoppingCart(@RequestBody ShoppingCartDto shoppingCart);

	@PostMapping("/api/v1/warehouse/add")
	void addProductToWarehouse(@RequestBody AddProductToWarehouseRequest request);

	@GetMapping("/api/v1/warehouse/address")
	AddressDto getWarehouseAddress();
}