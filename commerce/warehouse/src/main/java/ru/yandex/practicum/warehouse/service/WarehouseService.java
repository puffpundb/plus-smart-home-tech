package ru.yandex.practicum.warehouse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interaction_api.dto.*;
import ru.yandex.practicum.warehouse.entity.OrderBooking;
import ru.yandex.practicum.warehouse.entity.WarehouseProductEntity;
import ru.yandex.practicum.warehouse.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.warehouse.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.warehouse.exception.ProductInShoppingCartNotInWarehouse;
import ru.yandex.practicum.warehouse.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.warehouse.mapper.WarehouseMapper;
import ru.yandex.practicum.warehouse.repository.OrderBookingRepository;
import ru.yandex.practicum.warehouse.repository.WarehouseProductRepository;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseService {
	private final WarehouseProductRepository productRepository;
	private final OrderBookingRepository bookingRepository;

	private static final String[] ADDRESSES = {"ADDRESS_1", "ADDRESS_2"};
	private static final String CURRENT_ADDRESS =
			ADDRESSES[new SecureRandom().nextInt(ADDRESSES.length)];

	@Transactional
	public void newProductInWarehouse(NewProductInWarehouseRequest request) {
		if (productRepository.existsByProductId(request.getProductId())) {
			throw new SpecifiedProductAlreadyInWarehouseException(
					"Товар с ID " + request.getProductId() + " уже зарегистрирован на складе"
			);
		}
		WarehouseProductEntity entity = WarehouseMapper.toEntity(request);
		productRepository.save(entity);
		log.info("Новый товар {} добавлен на склад", request.getProductId());
	}

	@Transactional(readOnly = true)
	public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto cart) {
		return calculateBookingParams(cart.getProducts(), true);
	}

	@Transactional
	public void addProductToWarehouse(AddProductToWarehouseRequest request) {
		WarehouseProductEntity product = productRepository.findByProductId(request.getProductId())
				.orElseThrow(() -> new NoSpecifiedProductInWarehouseException(
						"Товар с ID " + request.getProductId() + " не найден на складе"
				));

		Long newQuantity = product.getQuantity() + request.getQuantity();
		product.setQuantity(newQuantity);
		productRepository.save(product);
		log.info("Остаток товара {} увеличен на {}. Новый остаток: {}",
				request.getProductId(), request.getQuantity(), newQuantity);
	}

	@Transactional(readOnly = true)
	public AddressDto getWarehouseAddress() {
		return WarehouseMapper.toAddressDto(CURRENT_ADDRESS);
	}

	@Transactional
	public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
		UUID orderId = request.getOrderId();
		Map<UUID, Long> products = request.getProducts();

		BookedProductsDto params = calculateBookingParams(products, true);

		for (Map.Entry<UUID, Long> entry : products.entrySet()) {
			UUID productId = entry.getKey();
			Long quantity = entry.getValue();

			WarehouseProductEntity product = productRepository.findByProductId(productId)
					.orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Товар " + productId + " не найден"));

			if (product.getQuantity() < quantity) {
				throw new ProductInShoppingCartLowQuantityInWarehouse(
						"Недостаточно товара " + productId + ". Доступно: " + product.getQuantity());
			}
			product.setQuantity(product.getQuantity() - quantity);
			productRepository.save(product);
		}

		List<WarehouseProductEntity> warehouseProducts = productRepository.findAllById(new ArrayList<>(products.keySet()));
		OrderBooking booking = WarehouseMapper.toBookingEntity(orderId, products, warehouseProducts,
				params.getDeliveryWeight(), params.getDeliveryVolume(), params.getFragile());

		bookingRepository.save(booking);
		log.info("Заказ {} собран. Создана бронь {}", orderId, booking.getBookingId());

		return params;
	}

	@Transactional
	public void shippedToDelivery(ShippedToDeliveryRequest request) {
		OrderBooking booking = bookingRepository.findByOrderId(request.getOrderId())
				.orElseThrow(() -> new RuntimeException("Бронь для заказа " + request.getOrderId() + " не найдена"));

		booking.setDeliveryId(request.getDeliveryId());
		bookingRepository.save(booking);
		log.info("Заказ {} передан в доставку {}", request.getOrderId(), request.getDeliveryId());
	}

	@Transactional
	public void acceptReturn(Map<UUID, Long> products) {
		for (Map.Entry<UUID, Long> entry : products.entrySet()) {
			UUID productId = entry.getKey();
			Long quantity = entry.getValue();

			WarehouseProductEntity product = productRepository.findByProductId(productId)
					.orElseThrow(() -> new NoSpecifiedProductInWarehouseException(
							"Товар " + productId + " не найден, нельзя вернуть"
					));

			product.setQuantity(product.getQuantity() + quantity);
			productRepository.save(product);
			log.info("Возврат: товар {} (+{} шт.) принят на склад", productId, quantity);
		}
	}

	private BookedProductsDto calculateBookingParams(Map<UUID, Long> products, boolean checkStock) {
		double totalWeight = 0.0;
		double totalVolume = 0.0;
		boolean hasFragile = false;

		List<UUID> productIds = new ArrayList<>(products.keySet());
		List<WarehouseProductEntity> entities = productRepository.findAllById(productIds);
		Map<UUID, WarehouseProductEntity> byId = entities.stream()
				.collect(Collectors.toMap(WarehouseProductEntity::getProductId, e -> e));

		for (Map.Entry<UUID, Long> entry : products.entrySet()) {
			UUID productId = entry.getKey();
			Long requestedQuantity = entry.getValue();

			WarehouseProductEntity product = byId.get(productId);
			if (product == null) {
				throw new ProductInShoppingCartNotInWarehouse(
						"Товар с ID " + productId + " не найден на складе"
				);
			}

			if (checkStock && product.getQuantity() < requestedQuantity) {
				throw new ProductInShoppingCartLowQuantityInWarehouse(
						"Недостаточно товара " + productId + ". Доступно: " +
								product.getQuantity() + ", требуется: " + requestedQuantity
				);
			}

			totalWeight += product.getWeight() * requestedQuantity;
			totalVolume += product.getWidth() * product.getHeight() * product.getDepth() * requestedQuantity;

			if (Boolean.TRUE.equals(product.getFragile())) {
				hasFragile = true;
			}
		}

		return BookedProductsDto.builder()
				.deliveryWeight(totalWeight)
				.deliveryVolume(totalVolume)
				.fragile(hasFragile)
				.build();
	}
}