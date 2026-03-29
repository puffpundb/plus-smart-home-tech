package ru.yandex.practicum.warehouse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interaction_api.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.interaction_api.dto.AddressDto;
import ru.yandex.practicum.interaction_api.dto.BookedProductsDto;
import ru.yandex.practicum.interaction_api.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.interaction_api.dto.ShoppingCartDto;
import ru.yandex.practicum.warehouse.entity.WarehouseProductEntity;
import ru.yandex.practicum.warehouse.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.warehouse.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.warehouse.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.warehouse.mapper.WarehouseMapper;
import ru.yandex.practicum.warehouse.repository.WarehouseProductRepository;

import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseService {

	private final WarehouseProductRepository repository;

	private static final String[] ADDRESSES = {"ADDRESS_1", "ADDRESS_2"};
	private static final String CURRENT_ADDRESS =
			ADDRESSES[new SecureRandom().nextInt(ADDRESSES.length)];

	@Transactional
	public void newProductInWarehouse(NewProductInWarehouseRequest request) {
		if (repository.existsByProductId(request.getProductId())) {
			throw new SpecifiedProductAlreadyInWarehouseException(
					"Товар с ID " + request.getProductId() + " уже зарегистрирован на складе"
			);
		}
		WarehouseProductEntity entity = WarehouseMapper.toEntity(request);
		repository.save(entity);
	}

	@Transactional(readOnly = true)
	public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto cart) {
		double totalWeight = 0.0;
		double totalVolume = 0.0;
		boolean hasFragile = false;

		for (Map.Entry<UUID, Long> entry : cart.getProducts().entrySet()) {
			UUID productId = entry.getKey();
			Long requestedQuantity = entry.getValue();

			WarehouseProductEntity product = repository.findByProductId(productId)
					.orElseThrow(() -> new NoSpecifiedProductInWarehouseException(
							"Товар с ID " + productId + " не найден на складе"
					));

			if (product.getQuantity() < requestedQuantity) {
				throw new ProductInShoppingCartLowQuantityInWarehouse(
						"Недостаточно товара " + productId + " на складе. Доступно: " +
								product.getQuantity() + ", требуется: " + requestedQuantity
				);
			}

			totalWeight += product.getWeight() * requestedQuantity;
			totalVolume += product.getWidth() * product.getHeight() * product.getDepth() * requestedQuantity;

			if (product.getFragile()) {
				hasFragile = true;
			}
		}

		return BookedProductsDto.builder()
				.deliveryWeight(totalWeight)
				.deliveryVolume(totalVolume)
				.fragile(hasFragile)
				.build();
	}

	@Transactional
	public void addProductToWarehouse(AddProductToWarehouseRequest request) {
		WarehouseProductEntity product = repository.findByProductId(request.getProductId())
				.orElseThrow(() -> new NoSpecifiedProductInWarehouseException(
						"Товар с ID " + request.getProductId() + " не найден на складе"
				));

		product.setQuantity(product.getQuantity() + request.getQuantity());
		repository.save(product);
	}

	@Transactional(readOnly = true)
	public AddressDto getWarehouseAddress() {
		return WarehouseMapper.toAddressDto(CURRENT_ADDRESS);
	}
}