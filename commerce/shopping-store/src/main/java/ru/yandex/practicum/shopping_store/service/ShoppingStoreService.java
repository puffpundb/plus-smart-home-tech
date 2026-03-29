package ru.yandex.practicum.shopping_store.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.shopping_store.dto.PageProductDto;
import ru.yandex.practicum.shopping_store.dto.ProductDto;
import ru.yandex.practicum.shopping_store.entity.ProductEntity;
import ru.yandex.practicum.shopping_store.entity.enums.ProductCategory;
import ru.yandex.practicum.shopping_store.entity.enums.ProductState;
import ru.yandex.practicum.shopping_store.entity.enums.QuantityState;
import ru.yandex.practicum.shopping_store.exception.ProductNotFoundException;
import ru.yandex.practicum.shopping_store.mapper.ShoppingStoreMapper;
import ru.yandex.practicum.shopping_store.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingStoreService {

	private final ProductRepository productRepository;

	@Transactional(readOnly = true)
	public PageProductDto getProductsByCategory(ProductCategory category,
												int page,
												int size,
												String[] sort) {

		String property = sort[0];
		Sort.Direction direction = Sort.Direction.fromString(sort[1]);

		Sort springSort = Sort.by(new Sort.Order(direction, property));
		Pageable pageable = PageRequest.of(page, size, springSort);

		Page<ProductEntity> entityPage = productRepository.findByProductCategoryAndProductState(category, ProductState.ACTIVE, pageable);

		return ShoppingStoreMapper.toPageProductDtoFromEntity(entityPage);
	}

	@Transactional(readOnly = true)
	public ProductDto getProductById(UUID id) {
		log.info("ShoppingStoreService: id - {}", id);

		return productRepository.findById(id)
				.map(ShoppingStoreMapper::toProductDto)
				.orElseThrow(() -> {
					log.warn("ShoppingStoreService: id - {} not found", id);
					return new ProductNotFoundException("Товар с ID " + id + " не найден");
				});
	}

	@Transactional
	public ProductDto createProduct(ProductDto dto) {
		log.info("ShoppingStoreService: Creating new product - {}, cat={}, state={}",
				dto.getProductName(), dto.getProductCategory(), dto.getProductState());

		ProductEntity entity = ShoppingStoreMapper.toProductEntity(dto);
		entity.setId(null);

		ProductEntity saved = productRepository.save(entity);
		log.info("ShoppingStoreService: Product created with id: {}", saved.getId());

		return ShoppingStoreMapper.toProductDto(saved);
	}

	@Transactional
	public ProductDto updateProduct(ProductDto dto) {
		log.info("ShoppingStoreService: Updating product - {}", dto.getProductId());

		if (dto.getProductId() == null) {
			throw new IllegalArgumentException("productId не может быть null при обновлении");
		}

		ProductEntity existing = productRepository.findById(dto.getProductId())
				.orElseThrow(() -> {
					log.warn("ShoppingStoreService: Product not found for update - {}", dto.getProductId());
					return new ProductNotFoundException("Товар с ID " + dto.getProductId() + " не найден");
				});

		existing.setProductName(dto.getProductName());
		existing.setDescription(dto.getDescription());
		existing.setImageSrc(dto.getImageSrc());
		existing.setProductCategory(dto.getProductCategory());
		existing.setPrice(dto.getPrice());

		ProductEntity updated = productRepository.save(existing);
		log.info("ShoppingStoreService: Product updated - {}", updated.getId());

		return ShoppingStoreMapper.toProductDto(updated);
	}

	@Transactional
	public boolean removeProductFromStore(UUID productId) {
		log.info("ShoppingStoreService: deactivated product - {}", productId);

		ProductEntity product = productRepository.findById(productId)
				.orElseThrow(() -> {
					log.warn("ShoppingStoreService: Product not found for removal - {}", productId);
					return new ProductNotFoundException("Товар с ID " + productId + " не найден");
				});

		product.setProductState(ProductState.DEACTIVATE);
		productRepository.save(product);
		log.info("ShoppingStoreService: Product deactivated - {}", productId);

		return true;
	}

	@Transactional
	public boolean setProductQuantityState(UUID productId, QuantityState quantityState) {
		log.info("ShoppingStoreService: Updating quantity state for product - {}, new state: {}",
				productId, quantityState);

		ProductEntity product = productRepository.findById(productId)
				.orElseThrow(() -> {
					log.warn("ShoppingStoreService: Product not found for quantity update - {}", productId);
					return new ProductNotFoundException("Товар с ID " + productId + " не найден");
				});

		product.setQuantityState(quantityState);
		productRepository.save(product);
		log.info("ShoppingStoreService: Quantity state updated for product - {}", productId);

		return true;
	}
}