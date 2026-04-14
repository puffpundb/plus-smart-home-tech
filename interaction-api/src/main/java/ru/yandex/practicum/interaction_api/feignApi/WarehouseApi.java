package ru.yandex.practicum.interaction_api.feignApi;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.interaction_api.dto.*;

import java.util.Map;
import java.util.UUID;

public interface WarehouseApi {
	@PutMapping("/api/v1/warehouse")
	void newProductInWarehouse(@RequestBody NewProductInWarehouseRequest request);

	@PostMapping("/api/v1/warehouse/shipped")
	void shippedToDelivery(@RequestBody ShippedToDeliveryRequest request);

	@PostMapping("/api/v1/warehouse/return")
	void acceptReturn(@RequestBody Map<UUID, Long> products);

	@PostMapping("/api/v1/warehouse/check")
	BookedProductsDto checkProductQuantityEnoughForShoppingCart(@RequestBody ShoppingCartDto shoppingCart);

	@PostMapping("/api/v1/warehouse/assembly")
	BookedProductsDto assemblyProductsForOrder(@RequestBody AssemblyProductsForOrderRequest request);

	@PostMapping("/api/v1/warehouse/add")
	void addProductToWarehouse(@RequestBody AddProductToWarehouseRequest request);

	@GetMapping("/api/v1/warehouse/address")
	AddressDto getWarehouseAddress();
}