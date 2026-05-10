package com.restaurant.inventory;

public record SupplySettingsItemResponse(
        String supplyId,
        String supplyCode,
        String supplyName,
        String unit,
        int reorderLevel,
        double marketPrice,
        String status
) {
}
