package com.restaurant.catalog.persistence.repository;

import com.restaurant.catalog.persistence.entity.IngredientEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<IngredientEntity, String> {

    Optional<IngredientEntity> findByTenantIdAndPropertyIdAndIngredientId(String tenantId, String propertyId, String ingredientId);

    List<IngredientEntity> findByTenantIdAndPropertyIdAndIngredientIdIn(String tenantId, String propertyId, Collection<String> ingredientIds);
}
