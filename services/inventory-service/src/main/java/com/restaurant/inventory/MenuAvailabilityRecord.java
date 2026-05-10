package com.restaurant.inventory;

public record MenuAvailabilityRecord(
        String itemId,
        String tenantId,
        String propertyId,
        boolean available,
        String reason
) {
}
