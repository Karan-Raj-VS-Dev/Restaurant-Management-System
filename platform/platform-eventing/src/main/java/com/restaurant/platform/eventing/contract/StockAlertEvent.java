package com.restaurant.platform.eventing.contract;

import java.time.Instant;

public record StockAlertEvent(
        String tenantId,
        String propertyId,
        String ingredientId,
        String ingredientName,
        String stockHealth,
        int availableQuantity,
        String unit,
        Instant occurredAt
) {
}
