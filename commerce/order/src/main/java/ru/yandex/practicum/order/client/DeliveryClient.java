package ru.yandex.practicum.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.interaction_api.feignApi.DeliveryApi;

@FeignClient(name = "delivery")
public interface DeliveryClient extends DeliveryApi {
}
