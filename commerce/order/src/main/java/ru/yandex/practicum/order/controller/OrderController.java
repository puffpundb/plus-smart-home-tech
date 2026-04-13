package ru.yandex.practicum.order.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.interaction_api.dto.CreateNewOrderRequest;
import ru.yandex.practicum.interaction_api.dto.OrderDto;
import ru.yandex.practicum.interaction_api.dto.ProductReturnRequest;
import ru.yandex.practicum.interaction_api.feignApi.OrderApi;
import ru.yandex.practicum.order.service.OrderService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OrderController implements OrderApi {
	private final OrderService orderService;

	@Override
	public List<OrderDto> getClientOrders(String username) {
		log.info("GET /api/v1/order?username={}", username);
		return orderService.getClientOrders(username);
	}

	@Override
	public OrderDto createNewOrder(CreateNewOrderRequest request) {
		log.info("PUT /api/v1/order - creating for cart: {}", request.getShoppingCart().getShoppingCartId());
		return orderService.createNewOrder(request);
	}

	@Override
	public OrderDto productReturn(ProductReturnRequest request) {
		log.info("POST /api/v1/order/return - order: {}", request.getOrderId());
		return orderService.productReturn(request);
	}

	@Override
	public OrderDto payment(UUID orderId) {
		log.info("POST /api/v1/order/payment - order: {}", orderId);
		return orderService.payment(orderId);
	}

	@Override
	public OrderDto paymentFailed(UUID orderId) {
		log.info("POST /api/v1/order/payment/failed - order: {}", orderId);
		return orderService.paymentFailed(orderId);
	}

	@Override
	public OrderDto paymentSuccess(UUID orderId) {
		log.info("POST /api/v1/order/payment/success - order: {}", orderId);
		return orderService.paymentSuccess(orderId);
	}

	@Override
	public OrderDto delivery(UUID orderId) {
		log.info("POST /api/v1/order/delivery - order: {}", orderId);
		return orderService.delivery(orderId);
	}

	@Override
	public OrderDto deliveryFailed(UUID orderId) {
		log.info("POST /api/v1/order/delivery/failed - order: {}", orderId);
		return orderService.deliveryFailed(orderId);
	}

	@Override
	public OrderDto complete(UUID orderId) {
		log.info("POST /api/v1/order/completed - order: {}", orderId);
		return orderService.complete(orderId);
	}

	@Override
	public OrderDto calculateTotalCost(UUID orderId) {
		log.info("POST /api/v1/order/calculate/total - order: {}", orderId);
		return orderService.calculateTotalCost(orderId);
	}

	@Override
	public OrderDto calculateDeliveryCost(UUID orderId) {
		log.info("POST /api/v1/order/calculate/delivery - order: {}", orderId);
		return orderService.calculateDeliveryCost(orderId);
	}

	@Override
	public OrderDto assembly(UUID orderId) {
		log.info("POST /api/v1/order/assembly - order: {}", orderId);
		return orderService.assembly(orderId);
	}

	@Override
	public OrderDto assemblyFailed(UUID orderId) {
		log.info("POST /api/v1/order/assembly/failed - order: {}", orderId);
		return orderService.assemblyFailed(orderId);
	}
}
