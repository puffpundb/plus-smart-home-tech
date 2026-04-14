package ru.yandex.practicum.delivery.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.delivery.service.DeliveryService;
import ru.yandex.practicum.interaction_api.dto.DeliveryDto;
import ru.yandex.practicum.interaction_api.dto.OrderDto;
import ru.yandex.practicum.interaction_api.feignApi.DeliveryApi;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
public class DeliveryController implements DeliveryApi {
	private final DeliveryService deliveryService;

	@Override
	public DeliveryDto planDelivery(@Valid DeliveryDto delivery) {
		log.info("PUT /api/v1/delivery - planning delivery for order: {}", delivery.getOrderId());
		return deliveryService.planDelivery(delivery);
	}

	@Override
	public Double deliveryCost(@Valid OrderDto order) {
		log.info("POST /api/v1/delivery/cost - calculating cost for order: {}", order.getOrderId());
		return deliveryService.calculateDeliveryCost(order);
	}

	@Override
	public void deliveryPicked(@NotNull UUID orderId) {
		log.info("POST /api/v1/delivery/picked - order {} picked up by courier", orderId);
		deliveryService.markAsShipped(orderId);
	}

	@Override
	public void deliverySuccessful(@NotNull UUID orderId) {
		log.info("POST /api/v1/delivery/successful - order {} delivered successfully", orderId);
		deliveryService.markAsDelivered(orderId);
	}

	@Override
	public void deliveryFailed(@NotNull UUID orderId) {
		log.error("POST /api/v1/delivery/failed - delivery failed for order {}", orderId);
		deliveryService.markAsDeliveryFailed(orderId);
	}
}
