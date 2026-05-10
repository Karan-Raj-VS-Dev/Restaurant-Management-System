package com.restaurant.takeaway;

import jakarta.validation.constraints.NotBlank;

public record CreateTakeawayOrderRequest(
        @NotBlank String propertyId,
        @NotBlank String channel
) {
}
