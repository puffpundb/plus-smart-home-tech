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

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {
	private final DeliveryRepository deliveryRepository;
	private final OrderClient orderClient;
	private final WarehouseClient warehouseClient;

	@Transactional
	public DeliveryDto planDelivery(DeliveryDto dto) {
		Delivery delivery = DeliveryMapper.toEntity(dto);
		Delivery saved = deliveryRepository.save(delivery);
		log.info("Доставка создана для заказа: {}, deliveryId: {}", saved.getOrderId(), saved.getDeliveryId());
		return DeliveryMapper.toDto(saved);
	}

	@Transactional(readOnly = true)
	public Double deliveryCost(OrderDto order) {
		double baseCost = 5.0;
		double sum = baseCost;

		// 1. Множитель склада (ADDRESS_1 -> 1, ADDRESS_2 -> 2)
		int warehouseFactor = 1;
		sum = (sum * warehouseFactor) + baseCost;

		// 2. Хрупкость
		if (Boolean.TRUE.equals(order.getFragile())) {
			sum = sum + (sum * 0.2);
		}

		// 3. Вес
		double weight = 0.0;
		if (order.getDeliveryWeight() != null) {
			weight = order.getDeliveryWeight();
		}
		sum = sum + (weight * 0.3);

		// 4. Объём
		double volume = 0.0;
		if (order.getDeliveryVolume() != null) {
			volume = order.getDeliveryVolume();
		}
		sum = sum + (volume * 0.2);

		// 5. Адрес доставки (если улица доставки != улице склада)
		boolean sameStreet = false;
		if (!sameStreet) {
			sum = sum + (sum * 0.2);
		}

		log.info("Рассчитана стоимость доставки для заказа {}: {}", order.getOrderId(), sum);
		return sum;
	}

	@Transactional
	public void deliveryPicked(UUID orderId) {
		Delivery delivery = deliveryRepository.findByOrderId(orderId)
				.orElseThrow(() -> new NoDeliveryFoundException("Доставка для заказа " + orderId + " не найдена"));

		delivery.setStatus(DeliveryState.IN_PROGRESS);
		deliveryRepository.save(delivery);
		log.info("Доставка {} переведена в IN_PROGRESS", orderId);

		// 1. Изменить статус заказа на ASSEMBLED
		orderClient.assembly(orderId);

		// 2. Связать доставку с учётной системой склада
		warehouseClient.shippedToDelivery(new ShippedToDeliveryRequest(delivery.getDeliveryId(), orderId));
	}

	@Transactional
	public void deliverySuccessful(UUID orderId) {
		Delivery delivery = deliveryRepository.findByOrderId(orderId)
				.orElseThrow(() -> new NoDeliveryFoundException("Доставка для заказа " + orderId + " не найдена"));

		delivery.setStatus(DeliveryState.DELIVERED);
		deliveryRepository.save(delivery);
		log.info("Доставка {} успешно завершена", orderId);

		orderClient.delivery(orderId);
	}

	@Transactional
	public void deliveryFailed(UUID orderId) {
		Delivery delivery = deliveryRepository.findByOrderId(orderId)
				.orElseThrow(() -> new NoDeliveryFoundException("Доставка для заказа " + orderId + " не найдена"));

		delivery.setStatus(DeliveryState.FAILED);
		deliveryRepository.save(delivery);
		log.info("Доставка {} завершилась ошибкой", orderId);

		orderClient.deliveryFailed(orderId);
	}
}
