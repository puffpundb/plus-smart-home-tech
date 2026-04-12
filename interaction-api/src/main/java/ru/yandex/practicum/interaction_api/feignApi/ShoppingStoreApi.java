package ru.yandex.practicum.interaction_api.feignApi;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface ShoppingStoreApi {
	@PostMapping("/api/v1/store/products/prices")
	Map<UUID, Double> getProductPrices(@RequestBody Set<UUID> productIds);
}
