package com.restaurant.billing.persistence.repository;

import com.restaurant.billing.persistence.entity.BillEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository extends JpaRepository<BillEntity, String> {

    Optional<BillEntity> findByOrderId(String orderId);

    List<BillEntity> findByTenantIdAndPropertyIdAndBillingStatus(String tenantId, String propertyId, String billingStatus);
}
