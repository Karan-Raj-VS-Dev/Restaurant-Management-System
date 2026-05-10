package com.restaurant.inventory;

public record IngredientSettingsItemResponse(
        String ingredientId,
        String ingredientCode,
        String ingredientName,
        String unit,
        int reorderThreshold,
        int maximumCapacity,
        double marketPrice,
        String status
) {
}
