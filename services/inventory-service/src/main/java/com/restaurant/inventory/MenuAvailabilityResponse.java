package com.restaurant.inventory;

public record MenuAvailabilityResponse(
        String itemId,
        String propertyId,
        boolean available,
        String reason
) {
}
