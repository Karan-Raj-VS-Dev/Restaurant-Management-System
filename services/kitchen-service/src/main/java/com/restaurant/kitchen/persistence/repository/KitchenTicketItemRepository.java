package com.restaurant.kitchen.persistence.repository;

import com.restaurant.kitchen.persistence.entity.KitchenTicketItemEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KitchenTicketItemRepository extends JpaRepository<KitchenTicketItemEntity, String> {

    List<KitchenTicketItemEntity> findByTicketIdOrderByOrderItemIdAsc(String ticketId);

    void deleteByTicketId(String ticketId);
}
