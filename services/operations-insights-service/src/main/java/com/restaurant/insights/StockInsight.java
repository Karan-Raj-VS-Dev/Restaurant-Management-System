package com.restaurant.insights;

public record StockInsight(
        String propertyId,
        String ingredientName,
        String stockHealth,
        int availableQuantity,
        String unit
) {
}
