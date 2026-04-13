package ru.yandex.practicum.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interaction_api.dto.OrderDto;
import ru.yandex.practicum.interaction_api.dto.PaymentDto;
import ru.yandex.practicum.payment.client.OrderClient;
import ru.yandex.practicum.payment.client.ShoppingStoreClient;
import ru.yandex.practicum.payment.entity.Payment;
import ru.yandex.practicum.payment.entity.enums.PaymentStatus;
import ru.yandex.practicum.payment.exception.NoOrderFoundException;
import ru.yandex.practicum.payment.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.payment.mapper.PaymentMapper;
import ru.yandex.practicum.payment.repository.PaymentRepository;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final ShoppingStoreClient shoppingStoreClient;
	private final OrderClient orderClient;

	@Transactional(readOnly = true)
	public Double productCost(OrderDto order) {
		if (order.getProducts() == null || order.getProducts().isEmpty()) {
			throw new NotEnoughInfoInOrderToCalculateException("В заказе нет товаров");
		}

		Map<UUID, Double> prices = shoppingStoreClient.getProductPrices(order.getProducts().keySet());

		double total = 0.0;
		for (Map.Entry<UUID, Long> entry : order.getProducts().entrySet()) {
			UUID productId = entry.getKey();
			Long quantity = entry.getValue();
			Double price = prices.get(productId);
			if (price == null) {
				throw new NotEnoughInfoInOrderToCalculateException("Цена товара " + productId + " не найдена");
			}
			total += price * quantity;
		}

		log.info("Рассчитана стоимость товаров для заказа {}: {}", order.getOrderId(), total);
		return total;
	}

	@Transactional(readOnly = true)
	public Double getTotalCost(OrderDto order) {
		Double productPrice = order.getProductPrice();
		if (productPrice == null) {
			productPrice = productCost(order);
		}

		Double deliveryPrice = order.getDeliveryPrice();
		if (deliveryPrice == null) {
			throw new NotEnoughInfoInOrderToCalculateException("Не указана стоимость доставки");
		}

		Double fee = productPrice * 0.1;
		Double total = productPrice + fee + deliveryPrice;

		log.info("Рассчитана итоговая стоимость для заказа {}: товары={}, доставка={}, НДС={}, итого={}",
				order.getOrderId(), productPrice, deliveryPrice, fee, total);
		return total;
	}

	@Transactional
	public PaymentDto payment(OrderDto order) {
		if (order.getOrderId() == null) {
			throw new NotEnoughInfoInOrderToCalculateException("Отсутствует идентификатор заказа");
		}

		if (paymentRepository.findByOrderId(order.getOrderId()).isPresent()) {
			throw new NotEnoughInfoInOrderToCalculateException("Оплата для заказа " + order.getOrderId() + " уже создана");
		}

		Double productPrice = order.getProductPrice();
		if (productPrice == null) {
			productPrice = productCost(order);
		}

		Double deliveryPrice = order.getDeliveryPrice();
		if (deliveryPrice == null) {
			throw new NotEnoughInfoInOrderToCalculateException("Не указана стоимость доставки");
		}

		Double feeTotal = productPrice * 0.1;
		Double totalPayment = productPrice + feeTotal + deliveryPrice;

		Payment payment = Payment.builder()
				.orderId(order.getOrderId())
				.status(PaymentStatus.PENDING)
				.totalPayment(totalPayment)
				.deliveryTotal(deliveryPrice)
				.feeTotal(feeTotal)
				.build();

		Payment saved = paymentRepository.save(payment);
		log.info("Оплата PENDING создана для заказа: {}, paymentId: {}", order.getOrderId(), saved.getPaymentId());

		return PaymentMapper.toDto(saved);
	}

	@Transactional
	public void paymentSuccess(UUID paymentId) {
		Payment payment = paymentRepository.findById(paymentId)
				.orElseThrow(() -> {
					log.error("Оплата не найдена с ID: {}", paymentId);
					return new NoOrderFoundException("Оплата с ID " + paymentId + " не найдена");
				});

		payment.setStatus(PaymentStatus.SUCCESS);
		paymentRepository.save(payment);
		log.info("Оплата {} переведена в статус SUCCESS", paymentId);

		orderClient.paymentSuccess(payment.getOrderId());
	}

	@Transactional
	public void paymentFailed(UUID paymentId) {
		Payment payment = paymentRepository.findById(paymentId)
				.orElseThrow(() -> {
					log.error("Оплата не найдена с ID: {}", paymentId);
					return new NoOrderFoundException("Оплата с ID " + paymentId + " не найдена");
				});

		payment.setStatus(PaymentStatus.FAILED);
		paymentRepository.save(payment);
		log.info("Оплата {} переведена в статус FAILED", paymentId);

		orderClient.paymentFailed(payment.getOrderId());
	}

	@Transactional
	public void notifyOrderPaymentSuccess(UUID orderId) {
		log.info("Оплата успешна {}", orderId);
		orderClient.paymentSuccess(orderId);
	}

	@Transactional
	public void notifyOrderPaymentFailed(UUID orderId) {
		log.error("Оплата не успешна {}", orderId);
		orderClient.paymentFailed(orderId);
	}

	@Transactional(readOnly = true)
	public Double calculateProductsCost(Set<UUID> productIds) {
		Map<UUID, Double> prices = shoppingStoreClient.getProductPrices(productIds);
		return prices.values().stream().mapToDouble(Double::doubleValue).sum();
	}
}
