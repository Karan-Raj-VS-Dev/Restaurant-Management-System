package com.restaurant.inventory;

public record IngredientStockRecord(
        String ingredientId,
        String tenantId,
        String propertyId,
        String ingredientName,
        int currentQuantity,
        String unit,
        int reorderThreshold,
        int maximumCapacity,
        double marketUnitPrice
) {
}
