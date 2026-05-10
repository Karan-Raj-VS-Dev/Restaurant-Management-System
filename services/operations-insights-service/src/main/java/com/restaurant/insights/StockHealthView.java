package com.restaurant.insights;

public record StockHealthView(
        String tenantId,
        String propertyId,
        String ingredientName,
        String stockHealth,
        int availableQuantity,
        String unit
) {
}
