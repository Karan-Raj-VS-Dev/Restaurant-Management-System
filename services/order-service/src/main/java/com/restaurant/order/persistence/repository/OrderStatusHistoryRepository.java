package com.restaurant.order.persistence.repository;

import com.restaurant.order.persistence.entity.OrderStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistoryEntity, String> {
}
