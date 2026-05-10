package com.restaurant.inventory.persistence.repository;

import com.restaurant.inventory.persistence.entity.CatalogRecipeIngredientEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogRecipeIngredientRepository extends JpaRepository<CatalogRecipeIngredientEntity, String> {

    List<CatalogRecipeIngredientEntity> findByMenuItemIdIn(Collection<String> menuItemIds);
}
