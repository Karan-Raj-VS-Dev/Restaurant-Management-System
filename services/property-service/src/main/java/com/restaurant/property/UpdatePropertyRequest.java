package com.restaurant.property;

public record UpdatePropertyRequest(
        String name,
        String addressLine,
        String city,
        String state,
        String country,
        String timezone,
        Double latitude,
        Double longitude,
        String status
) {
}
