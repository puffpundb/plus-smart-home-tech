package ru.yandex.practicum.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interaction_api.dto.*;
import ru.yandex.practicum.interaction_api.enums.OrderStatus;
import ru.yandex.practicum.order.client.DeliveryClient;
import ru.yandex.practicum.order.client.PaymentClient;
import ru.yandex.practicum.order.client.WarehouseClient;
import ru.yandex.practicum.order.entity.Order;
import ru.yandex.practicum.order.exception.NoOrderFoundException;
import ru.yandex.practicum.order.mapper.OrderMapper;
import ru.yandex.practicum.order.repository.OrderRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
	private final OrderRepository orderRepository;
	private final WarehouseClient warehouseClient;
	private final DeliveryClient deliveryClient;
	private final PaymentClient paymentClient;

	@Transactional(readOnly = true)
	public List<OrderDto> getClientOrders(String username) {
		if (username == null || username.isBlank()) {
			throw new IllegalArgumentException("Имя пользователя не должно быть пустым");
		}
		log.debug("Fetching orders for user: {}", username);
		return OrderMapper.toDtoList(orderRepository.findByUsername(username));
	}

	@Transactional
	public OrderDto createNewOrder(CreateNewOrderRequest request) {
		log.info("Creating order for cart: {}", request.getShoppingCart().getShoppingCartId());

		Order order = OrderMapper.toEntity(request);
		order.setState(OrderStatus.NEW);
		Order savedOrder = orderRepository.save(order);

		AssemblyProductsForOrderRequest assemblyRequest = AssemblyProductsForOrderRequest.builder()
				.orderId(savedOrder.getOrderId())
				.products(request.getShoppingCart().getProducts())
				.build();
		warehouseClient.assemblyProductsForOrder(assemblyRequest);

		AddressDto warehouseAddr = warehouseClient.getWarehouseAddress();

		DeliveryDto deliveryRequest = DeliveryDto.builder()
				.orderId(savedOrder.getOrderId())
				.fromAddress(warehouseAddr)
				.toAddress(request.getDeliveryAddress())
				.build();

		DeliveryDto plannedDelivery = deliveryClient.planDelivery(deliveryRequest);

		savedOrder.setDeliveryId(plannedDelivery.getDeliveryId());
//		orderRepository.save(savedOrder); Нашел информацию, что Hibernate должен сделать UPDATE после транзакции
//		если найдет расхождения в объектах. Иначе не совсем понимаю как избавиться. У меня UUID генерируется на уровне БД

		log.info("Order created successfully: {}, deliveryId: {}", savedOrder.getOrderId(), plannedDelivery.getDeliveryId());
		return OrderMapper.toDto(savedOrder);
	}

	@Transactional
	public OrderDto assembly(UUID orderId) {
		Order order = findOrderOrThrow(orderId);
		order.setState(OrderStatus.ASSEMBLED);
		log.info("Order {} status changed to ASSEMBLED", orderId);
		return OrderMapper.toDto(orderRepository.save(order));
	}

	@Transactional
	public OrderDto assemblyFailed(UUID orderId) {
		Order order = findOrderOrThrow(orderId);
		order.setState(OrderStatus.ASSEMBLY_FAILED);
		log.info("Order {} status changed to ASSEMBLY_FAILED", orderId);
		return OrderMapper.toDto(orderRepository.save(order));
	}

	@Transactional
	public OrderDto calculateDeliveryCost(UUID orderId) {
		Order order = findOrderOrThrow(orderId);
		OrderDto orderDto = OrderMapper.toDto(order);

		Double cost = deliveryClient.deliveryCost(orderDto);
		order.setDeliveryPrice(cost);

		log.info("Delivery cost calculated for order {}: {}", orderId, cost);
		return OrderMapper.toDto(orderRepository.save(order));
	}

	@Transactional
	public OrderDto calculateTotalCost(UUID orderId) {
		Order order = findOrderOrThrow(orderId);
		OrderDto orderDto = OrderMapper.toDto(order);

		Double productCost = paymentClient.productCost(orderDto);
		order.setProductPrice(productCost);

		Double totalCost = paymentClient.getTotalCost(orderDto);
		order.setTotalPrice(totalCost);

		orderDto.setProductPrice(productCost);
		orderDto.setTotalPrice(totalCost);

		PaymentDto paymentResult = paymentClient.payment(orderDto);

		order.setPaymentId(paymentResult.getPaymentId());

		log.info("Total cost calculated for order {}: product={}, total={}, paymentId={}",
				orderId, productCost, totalCost, paymentResult.getPaymentId());

		Order savedOrder = orderRepository.save(order);
		return OrderMapper.toDto(savedOrder);
	}

	@Transactional
	public OrderDto payment(UUID orderId) {
		Order order = findOrderOrThrow(orderId);
		OrderDto orderDto = OrderMapper.toDto(order);

		PaymentDto paymentResult = paymentClient.payment(orderDto);
		order.setPaymentId(paymentResult.getPaymentId());
		order.setState(OrderStatus.PAID);

		log.info("Payment processed for order {}, paymentId: {}", orderId, paymentResult.getPaymentId());
		return OrderMapper.toDto(orderRepository.save(order));
	}

	@Transactional
	public OrderDto paymentFailed(UUID orderId) {
		Order order = findOrderOrThrow(orderId);
		order.setState(OrderStatus.PAYMENT_FAILED);
		log.info("Order {} marked as PAYMENT_FAILED", orderId);
		return OrderMapper.toDto(orderRepository.save(order));
	}

	@Transactional
	public OrderDto paymentSuccess(UUID orderId) {
		Order order = findOrderOrThrow(orderId);
		order.setState(OrderStatus.PAID);
		log.info("Order {} marked as PAID via paymentSuccess callback", orderId);
		return OrderMapper.toDto(orderRepository.save(order));
	}

	@Transactional
	public OrderDto delivery(UUID orderId) {
		Order order = findOrderOrThrow(orderId);
		order.setState(OrderStatus.DELIVERED);
		log.info("Order {} marked as DELIVERED", orderId);
		return OrderMapper.toDto(orderRepository.save(order));
	}

	@Transactional
	public OrderDto deliveryFailed(UUID orderId) {
		Order order = findOrderOrThrow(orderId);
		order.setState(OrderStatus.DELIVERY_FAILED);
		log.info("Order {} marked as DELIVERY_FAILED", orderId);
		return OrderMapper.toDto(orderRepository.save(order));
	}

	@Transactional
	public OrderDto complete(UUID orderId) {
		Order order = findOrderOrThrow(orderId);
		order.setState(OrderStatus.COMPLETED);
		log.info("Order {} marked as COMPLETED", orderId);
		return OrderMapper.toDto(orderRepository.save(order));
	}

	@Transactional
	public OrderDto productReturn(ProductReturnRequest request) {
		Order order = findOrderOrThrow(request.getOrderId());

		warehouseClient.acceptReturn(request.getProducts());

		order.setState(OrderStatus.PRODUCT_RETURNED);
		log.info("Return processed for order {}", request.getOrderId());
		return OrderMapper.toDto(orderRepository.save(order));
	}

	private Order findOrderOrThrow(UUID orderId) {
		return orderRepository.findById(orderId)
				.orElseThrow(() -> {
					log.error("Order not found with ID: {}", orderId);
					return new NoOrderFoundException("Заказ с ID " + orderId + " не найден");
				});
	}
}
