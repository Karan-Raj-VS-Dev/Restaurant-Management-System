package com.restaurant.table.persistence.repository;

import com.restaurant.table.persistence.entity.RestaurantTableEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTableEntity, String> {

    List<RestaurantTableEntity> findByTenantIdAndPropertyIdAndStatus(String tenantId, String propertyId, String status);

    List<RestaurantTableEntity> findByTenantIdAndPropertyIdAndActiveTrue(String tenantId, String propertyId);

    Optional<RestaurantTableEntity> findByTenantIdAndPropertyIdAndTableId(String tenantId, String propertyId, String tableId);
}
