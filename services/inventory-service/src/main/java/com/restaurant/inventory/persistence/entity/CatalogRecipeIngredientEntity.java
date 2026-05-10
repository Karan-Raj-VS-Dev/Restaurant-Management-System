package com.restaurant.inventory.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(schema = "catalog", name = "recipe_ingredients")
public class CatalogRecipeIngredientEntity {

    @Id
    @Column(name = "recipe_ingredient_id", nullable = false, length = 64)
    private String recipeIngredientId;

    @Column(name = "menu_item_id", nullable = false, length = 64)
    private String menuItemId;

    @Column(name = "ingredient_id", nullable = false, length = 64)
    private String ingredientId;

    @Column(name = "quantity_required", nullable = false, precision = 14, scale = 3)
    private BigDecimal quantityRequired;

    protected CatalogRecipeIngredientEntity() {
    }

    public String getRecipeIngredientId() {
        return recipeIngredientId;
    }

    public String getMenuItemId() {
        return menuItemId;
    }

    public String getIngredientId() {
        return ingredientId;
    }

    public BigDecimal getQuantityRequired() {
        return quantityRequired;
    }
}
