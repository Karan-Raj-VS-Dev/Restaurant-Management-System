package com.restaurant.property;

public record PropertyResponse(
        String tenantId,
        String productSlug,
        String propertyId,
        String name,
        String city,
        String state,
        String country,
        String addressLine,
        Double latitude,
        Double longitude,
        String status
) {
}
