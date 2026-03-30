package ru.yandex.practicum.shopping_store.repository;

import ru.yandex.practicum.shopping_store.entity.ProductEntity;
import ru.yandex.practicum.shopping_store.entity.enums.ProductCategory;
import ru.yandex.practicum.shopping_store.entity.enums.ProductState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {
//	Page<ProductEntity> findByProductCategoryAndProductState(ProductCategory category,
//															 ProductState state,
//															 Pageable pageable);

	Page<ProductEntity> findByProductCategoryAndProductState(ProductCategory category,
															 Pageable pageable);

	Optional<ProductEntity> findByIdAndProductState(UUID id, ProductState state);
}
