package com.restaurant.inventory;

public record StockItemResponse(
        String ingredientId,
        String propertyId,
        String ingredientName,
        int onHandQuantity,
        String unit,
        int reorderThreshold,
        int maximumCapacity,
        String stockHealth
) {
}
