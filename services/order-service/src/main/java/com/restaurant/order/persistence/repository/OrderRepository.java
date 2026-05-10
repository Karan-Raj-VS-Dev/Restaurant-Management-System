package com.restaurant.order.persistence.repository;

import com.restaurant.order.persistence.entity.OrderEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, String> {

    List<OrderEntity> findByTenantIdAndPropertyIdAndOrderStatus(String tenantId, String propertyId, String orderStatus);

    List<OrderEntity> findByWaiterId(String waiterId);
}
