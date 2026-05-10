package com.restaurant.inventory.persistence.repository;

import com.restaurant.inventory.persistence.entity.StockItemEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockItemRepository extends JpaRepository<StockItemEntity, String> {

    List<StockItemEntity> findByTenantIdAndPropertyId(String tenantId, String propertyId);

    List<StockItemEntity> findByTenantIdAndPropertyIdOrderByIngredientNameAsc(String tenantId, String propertyId);

    Optional<StockItemEntity> findByTenantIdAndPropertyIdAndIngredientId(String tenantId, String propertyId, String ingredientId);
}
