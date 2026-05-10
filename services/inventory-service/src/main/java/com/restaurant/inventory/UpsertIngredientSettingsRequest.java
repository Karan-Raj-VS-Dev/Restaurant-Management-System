package com.restaurant.inventory;

public record UpsertIngredientSettingsRequest(
        String ingredientCode,
        String ingredientName,
        String unit,
        int reorderThreshold,
        int maximumCapacity,
        double marketPrice,
        String status
) {
}
