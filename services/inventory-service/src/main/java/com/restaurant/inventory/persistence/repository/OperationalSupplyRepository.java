package com.restaurant.inventory.persistence.repository;

import com.restaurant.inventory.persistence.entity.OperationalSupplyEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationalSupplyRepository extends JpaRepository<OperationalSupplyEntity, String> {

    List<OperationalSupplyEntity> findByTenantIdAndPropertyIdOrderBySupplyNameAsc(String tenantId, String propertyId);

    Optional<OperationalSupplyEntity> findByTenantIdAndPropertyIdAndSupplyId(String tenantId, String propertyId, String supplyId);
}
