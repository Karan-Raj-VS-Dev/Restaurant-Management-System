package com.restaurant.catalog;

public record RecipeIngredientInput(
        String ingredientId,
        String ingredientName,
        String quantity
) {
}
