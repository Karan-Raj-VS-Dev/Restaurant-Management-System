package com.restaurant.inventory.persistence.repository;

import com.restaurant.inventory.persistence.entity.CatalogIngredientEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogIngredientRepository extends JpaRepository<CatalogIngredientEntity, String> {

    Optional<CatalogIngredientEntity> findByTenantIdAndPropertyIdAndIngredientId(String tenantId, String propertyId, String ingredientId);

    List<CatalogIngredientEntity> findByTenantIdAndPropertyIdAndIngredientIdIn(String tenantId, String propertyId, Collection<String> ingredientIds);
}
