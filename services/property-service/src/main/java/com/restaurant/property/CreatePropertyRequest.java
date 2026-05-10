package com.restaurant.property;

import jakarta.validation.constraints.NotBlank;

public record CreatePropertyRequest(
        @NotBlank(message = "Property name is required")
        String name,
        @NotBlank(message = "Address is required")
        String addressLine,
        @NotBlank(message = "City is required")
        String city,
        String state,
        String country,
        String timezone,
        Double latitude,
        Double longitude
) {
}
