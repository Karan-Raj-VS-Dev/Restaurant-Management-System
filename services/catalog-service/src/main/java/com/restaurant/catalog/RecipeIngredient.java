package com.restaurant.catalog;

public record RecipeIngredient(
        String ingredientId,
        String name,
        String quantity
) {
}
