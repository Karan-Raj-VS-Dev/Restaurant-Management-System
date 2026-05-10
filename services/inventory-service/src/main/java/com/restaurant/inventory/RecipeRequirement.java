package com.restaurant.inventory;

public record RecipeRequirement(
        String ingredientId,
        String ingredientName,
        int quantity,
        String unit
) {
}
