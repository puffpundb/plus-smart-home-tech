package ru.yandex.practicum.warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.warehouse.entity.OrderBookingItem;

@Repository
public interface OrderBookingItemRepository extends JpaRepository<OrderBookingItem, Long> {
}
