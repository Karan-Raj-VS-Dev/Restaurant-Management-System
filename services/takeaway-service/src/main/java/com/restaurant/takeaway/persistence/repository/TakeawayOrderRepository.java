package com.restaurant.takeaway.persistence.repository;

import com.restaurant.takeaway.persistence.entity.TakeawayOrderEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TakeawayOrderRepository extends JpaRepository<TakeawayOrderEntity, String> {

    List<TakeawayOrderEntity> findByTenantIdAndPropertyIdAndTakeawayStatus(String tenantId, String propertyId, String takeawayStatus);

    List<TakeawayOrderEntity> findBySourceChannel(String sourceChannel);
}
