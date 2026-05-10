package com.restaurant.inventory;

public record InventoryStockAdjustmentRequest(
        String ingredientId,
        int quantityDelta
) {
}
