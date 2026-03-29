package ru.yandex.practicum.shopping_cart.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.interaction_api.warehouse.WarehouseApi;

@FeignClient(name = "warehouse")
public interface WarehouseClient extends WarehouseApi {
}