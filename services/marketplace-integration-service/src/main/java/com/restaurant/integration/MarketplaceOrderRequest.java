package com.restaurant.integration;

import jakarta.validation.constraints.NotBlank;

public record MarketplaceOrderRequest(
        @NotBlank String provider,
        @NotBlank String externalOrderId,
        @NotBlank String propertyId
) {
}
