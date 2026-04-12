package ru.yandex.practicum.payment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.interaction_api.dto.OrderDto;
import ru.yandex.practicum.interaction_api.dto.PaymentDto;
import ru.yandex.practicum.interaction_api.feignApi.PaymentApi;
import ru.yandex.practicum.payment.service.PaymentService;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {
	private final PaymentService paymentService;

	@Override
	public PaymentDto payment(OrderDto order) {
		log.info("POST /api/v1/payment - creating payment for order: {}", order.getOrderId());
		return paymentService.payment(order);
	}

	@Override
	public Double getTotalCost(OrderDto order) {
		log.info("POST /api/v1/payment/totalCost - calculating total cost for order: {}", order.getOrderId());
		return paymentService.getTotalCost(order);
	}

	@Override
	public void paymentSuccess(UUID paymentId) {
		log.info("POST /api/v1/payment/refund - marking payment {} as SUCCESS", paymentId);
		paymentService.paymentSuccess(paymentId);
	}

	@Override
	public Double productCost(OrderDto order) {
		log.info("POST /api/v1/payment/productCost - calculating product cost for order: {}", order.getOrderId());
		return paymentService.productCost(order);
	}

	@Override
	public void paymentFailed(UUID paymentId) {
		log.info("POST /api/v1/payment/failed - marking payment {} as FAILED", paymentId);
		paymentService.paymentFailed(paymentId);
	}
}
