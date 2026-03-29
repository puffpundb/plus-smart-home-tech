package ru.yandex.practicum.warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.warehouse.entity.WarehouseProductEntity;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WarehouseProductRepository extends JpaRepository<WarehouseProductEntity, UUID> {

	Optional<WarehouseProductEntity> findByProductId(UUID productId);

	boolean existsByProductId(UUID productId);
}