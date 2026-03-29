package ru.yandex.practicum.shopping_cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.shopping_cart.entity.CartItemEntity;

import java.util.List;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItemEntity, UUID> {

	List<CartItemEntity> findByCartId(UUID cartId);

	List<CartItemEntity> findByCartIdAndProductIdIn(UUID cartId, List<UUID> productIds);

	void deleteByCartIdAndProductIdIn(UUID cartId, List<UUID> productIds);
}