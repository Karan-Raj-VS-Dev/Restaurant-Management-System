package com.restaurant.catalog.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "recipe_ingredients")
public class RecipeIngredientEntity {

    @Id
    @Column(name = "recipe_ingredient_id", nullable = false, length = 64)
    private String recipeIngredientId;

    @Column(name = "menu_item_id", nullable = false, length = 64)
    private String menuItemId;

    @Column(name = "ingredient_id", nullable = false, length = 64)
    private String ingredientId;

    @Column(name = "quantity_required", nullable = false, precision = 14, scale = 3)
    private BigDecimal quantityRequired;

    @Column(name = "wastage_factor", nullable = false, precision = 6, scale = 3)
    private BigDecimal wastageFactor;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public RecipeIngredientEntity() {
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    @PreUpdate
    void onUpdate() {
        // no-op; updated_at is not stored on this table
    }

    public String getRecipeIngredientId() {
        return recipeIngredientId;
    }

    public void setRecipeIngredientId(String recipeIngredientId) {
        this.recipeIngredientId = recipeIngredientId;
    }

    public String getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
    }

    public String getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(String ingredientId) {
        this.ingredientId = ingredientId;
    }

    public BigDecimal getQuantityRequired() {
        return quantityRequired;
    }

    public void setQuantityRequired(BigDecimal quantityRequired) {
        this.quantityRequired = quantityRequired;
    }

    public BigDecimal getWastageFactor() {
        return wastageFactor;
    }

    public void setWastageFactor(BigDecimal wastageFactor) {
        this.wastageFactor = wastageFactor;
    }
}
