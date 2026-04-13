package ru.yandex.practicum.interaction_api.feignApi;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.interaction_api.dto.DeliveryDto;
import ru.yandex.practicum.interaction_api.dto.OrderDto;

import java.util.UUID;

public interface DeliveryApi {
	@PutMapping("/api/v1/delivery")
	DeliveryDto planDelivery(@RequestBody DeliveryDto delivery);

	@PostMapping("/api/v1/delivery/cost")
	Double deliveryCost(@RequestBody OrderDto order);

	@PostMapping("/api/v1/delivery/picked")
	void deliveryPicked(@RequestBody UUID orderId);

	@PostMapping("/api/v1/delivery/successful")
	void deliverySuccessful(@RequestBody UUID orderId);

	@PostMapping("/api/v1/delivery/failed")
	void deliveryFailed(@RequestBody UUID orderId);
}
