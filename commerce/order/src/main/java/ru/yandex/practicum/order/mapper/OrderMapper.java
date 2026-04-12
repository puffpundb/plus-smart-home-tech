package ru.yandex.practicum.order.mapper;

import ru.yandex.practicum.interaction_api.dto.CreateNewOrderRequest;
import ru.yandex.practicum.interaction_api.dto.OrderDto;
import ru.yandex.practicum.order.entity.Order;
import ru.yandex.practicum.order.entity.OrderItem;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class OrderMapper {
	public static OrderDto toDto(Order order) {
		if (order == null) return null;

		Map<UUID, Long> products = order.getItems().stream()
				.collect(Collectors.toMap(
						OrderItem::getProductId,
						OrderItem::getQuantity
				));

		return OrderDto.builder()
				.orderId(order.getOrderId())
				.shoppingCartId(order.getShoppingCartId())
				.products(products)
				.paymentId(order.getPaymentId())
				.deliveryId(order.getDeliveryId())
				.state(order.getState())
				.deliveryWeight(order.getDeliveryWeight())
				.deliveryVolume(order.getDeliveryVolume())
				.fragile(order.getFragile())
				.totalPrice(order.getTotalPrice())
				.deliveryPrice(order.getDeliveryPrice())
				.productPrice(order.getProductPrice())
				.build();
	}

	public static List<OrderDto> toDtoList(List<Order> orders) {
		return orders.stream()
				.map(OrderMapper::toDto)
				.collect(Collectors.toList());
	}

	public static Order toEntity(CreateNewOrderRequest request) {
		if (request == null) return null;

		Order order = Order.builder()
				.orderId(UUID.randomUUID())
				.shoppingCartId(request.getShoppingCart().getShoppingCartId())
				.deliveryWeight(0.0)
				.deliveryVolume(0.0)
				.fragile(false)
				.build();

		if (request.getShoppingCart().getProducts() != null) {
			for (Map.Entry<UUID, Long> entry : request.getShoppingCart().getProducts().entrySet()) {
				OrderItem item = OrderItem.builder()
						.productId(UUID.fromString(String.valueOf(entry.getKey())))
						.quantity(entry.getValue())
						.price(0.0)
						.order(order)
						.build();
				order.getItems().add(item);
			}
		}
		return order;
	}
}
