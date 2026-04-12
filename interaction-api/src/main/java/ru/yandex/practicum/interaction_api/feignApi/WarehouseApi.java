package ru.yandex.practicum.interaction_api.feignApi;

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

	@PostMapping("/api/v1/warehouse/assembly")
	void assemblyProductForOrderFromShoppingCart(@RequestBody ShoppingCartDto cart);

	@PostMapping("/api/v1/warehouse/shipped")
	void shippedToDelivery(@RequestBody ShippedToDeliveryRequest request);

	@PostMapping("/api/v1/warehouse/return")
	void returnProducts(@RequestBody ProductReturnRequest request);
}