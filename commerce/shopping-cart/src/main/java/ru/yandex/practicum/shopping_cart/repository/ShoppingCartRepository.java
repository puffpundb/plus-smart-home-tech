package ru.yandex.practicum.shopping_cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.shopping_cart.entity.ShoppingCartEntity;

import java.util.Optional;
import java.util.UUID;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCartEntity, UUID> {

	Optional<ShoppingCartEntity> findByUsernameAndIsActive(String username, boolean isActive);
}