package com.restaurant.inventory;

public record UpsertSupplySettingsRequest(
        String supplyCode,
        String supplyName,
        String unit,
        int reorderLevel,
        double marketPrice,
        String status
) {
}
