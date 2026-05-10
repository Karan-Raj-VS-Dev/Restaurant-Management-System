package com.restaurant.catalog.persistence.repository;

import com.restaurant.catalog.persistence.entity.RecipeIngredientEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredientEntity, String> {

    List<RecipeIngredientEntity> findByMenuItemIdIn(Collection<String> menuItemIds);

    @Modifying
    void deleteByMenuItemId(String menuItemId);
}
