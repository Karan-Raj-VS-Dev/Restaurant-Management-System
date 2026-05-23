package com.restaurant.order.persistence.repository;

import com.restaurant.order.persistence.entity.OrderItemEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, String> {

    List<OrderItemEntity> findByOrderIdIn(Collection<String> orderIds);

    List<OrderItemEntity> findByOrderIdOrderByCreatedAtAsc(String orderId);

    void deleteByOrderId(String orderId);
}
