package com.restaurant.inventory.persistence.repository;

import com.restaurant.inventory.persistence.entity.CatalogMenuItemEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogMenuItemRepository extends JpaRepository<CatalogMenuItemEntity, String> {

    List<CatalogMenuItemEntity> findByTenantIdAndPropertyIdAndActiveTrueOrderByItemNameAsc(String tenantId, String propertyId);
}
