package com.restaurant.kitchen.persistence.repository;

import com.restaurant.kitchen.persistence.entity.KitchenTicketEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KitchenTicketRepository extends JpaRepository<KitchenTicketEntity, String> {

    Optional<KitchenTicketEntity> findByOrderId(String orderId);

    List<KitchenTicketEntity> findByTenantIdAndPropertyIdAndTicketStatus(String tenantId, String propertyId, String ticketStatus);
}
