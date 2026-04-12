package ru.yandex.practicum.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.interaction_api.feignApi.PaymentApi;

@FeignClient(name = "payment")
public interface PaymentClient extends PaymentApi {
}
