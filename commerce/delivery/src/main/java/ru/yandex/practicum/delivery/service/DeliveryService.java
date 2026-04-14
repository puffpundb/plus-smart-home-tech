package ru.yandex.practicum.delivery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.delivery.client.OrderClient;
import ru.yandex.practicum.delivery.client.WarehouseClient;
import ru.yandex.practicum.delivery.entity.Delivery;
import ru.yandex.practicum.delivery.excaption.NoDeliveryFoundException;
import ru.yandex.practicum.delivery.mapper.DeliveryMapper;
import ru.yandex.practicum.delivery.repository.DeliveryRepository;
import ru.yandex.practicum.interaction_api.dto.DeliveryDto;
import ru.yandex.practicum.interaction_api.dto.OrderDto;
import ru.yandex.practicum.interaction_api.dto.ShippedToDeliveryRequest;
import ru.yandex.practicum.interaction_api.enums.DeliveryState;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {
	private final DeliveryRepository deliveryRepository;
	private final OrderClient orderClient;
	private final WarehouseClient warehouseClient;

	private static final BigDecimal BASE_COST = BigDecimal.valueOf(5.0);
	private static final BigDecimal WAREHOUSE_FACTOR_ADDRESS_1 = BigDecimal.ONE;
	private static final BigDecimal WAREHOUSE_FACTOR_ADDRESS_2 = BigDecimal.valueOf(2);
	private static final BigDecimal FRAGILE_SURCHARGE_RATE = BigDecimal.valueOf(0.2);
	private static final BigDecimal WEIGHT_RATE = BigDecimal.valueOf(0.3);
	private static final BigDecimal VOLUME_RATE = BigDecimal.valueOf(0.2);
	private static final BigDecimal DIFFERENT_STREET_SURCHARGE_RATE = BigDecimal.valueOf(0.2);

	@Transactional
	public DeliveryDto planDelivery(DeliveryDto dto) {
		Delivery delivery = DeliveryMapper.toEntity(dto);
		Delivery saved = deliveryRepository.save(delivery);
		log.info("Доставка создана для заказа: {}, deliveryId: {}", saved.getOrderId(), saved.getDeliveryId());
		return DeliveryMapper.toDto(saved);
	}

	@Transactional(readOnly = true)
	public Double calculateDeliveryCost(OrderDto order) {
		Delivery delivery = deliveryRepository.findByOrderId(order.getOrderId())
				.orElseThrow(() -> new NoDeliveryFoundException("Доставка для заказа " + order.getOrderId() + " не найдена"));

		BigDecimal sum = BASE_COST;

		BigDecimal warehouseFactor;
		if (delivery.getFromCity() != null && delivery.getFromCity().contains("ADDRESS_2")) {
			warehouseFactor = WAREHOUSE_FACTOR_ADDRESS_2;
		} else {
			warehouseFactor = WAREHOUSE_FACTOR_ADDRESS_1;
		}
		sum = sum.add(sum.multiply(warehouseFactor));

		if (Boolean.TRUE.equals(delivery.getFragile())) sum = sum.add(sum.multiply(FRAGILE_SURCHARGE_RATE));

		sum = sum.add(BigDecimal.valueOf(delivery.getWeight()).multiply(WEIGHT_RATE));

		sum = sum.add(BigDecimal.valueOf(delivery.getVolume()).multiply(VOLUME_RATE));

		String fromStreet = delivery.getFromStreet();
		String toStreet = delivery.getToStreet();

		if (!fromStreet.equals(toStreet)) sum = sum.add(sum.multiply(DIFFERENT_STREET_SURCHARGE_RATE));

		log.info("Рассчитана стоимость доставки для заказа {}: {}", order.getOrderId(), sum);
		return sum.doubleValue();
	}

	@Transactional
	public void markAsShipped(UUID orderId) {
		Delivery delivery = deliveryRepository.findByOrderId(orderId)
				.orElseThrow(() -> new NoDeliveryFoundException("Доставка для заказа " + orderId + " не найдена"));

		delivery.setStatus(DeliveryState.IN_PROGRESS);
		deliveryRepository.save(delivery);
		log.info("Доставка {} переведена в статус IN_PROGRESS", orderId);

		warehouseClient.shippedToDelivery(
				ShippedToDeliveryRequest.builder()
						.orderId(orderId)
						.deliveryId(delivery.getDeliveryId())
						.build()
		);

		orderClient.assembly(orderId);
	}

	@Transactional
	public void markAsDelivered(UUID orderId) {
		Delivery delivery = deliveryRepository.findByOrderId(orderId)
				.orElseThrow(() -> new NoDeliveryFoundException("Доставка для заказа " + orderId + " не найдена"));

		delivery.setStatus(DeliveryState.DELIVERED);
		deliveryRepository.save(delivery);
		log.info("Доставка {} успешно завершена", orderId);

		orderClient.delivery(orderId);
	}

	@Transactional
	public void markAsDeliveryFailed(UUID orderId) {
		Delivery delivery = deliveryRepository.findByOrderId(orderId)
				.orElseThrow(() -> new NoDeliveryFoundException("Доставка для заказа " + orderId + " не найдена"));

		delivery.setStatus(DeliveryState.FAILED);
		deliveryRepository.save(delivery);
		log.error("Доставка {} завершилась ошибкой", orderId);

		orderClient.deliveryFailed(orderId);
	}
}
