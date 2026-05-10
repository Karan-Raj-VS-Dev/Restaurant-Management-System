package com.restaurant.integration;

import java.time.Instant;

public record MarketplaceOrderResponse(
        String integrationRequestId,
        String tenantId,
        String propertyId,
        String provider,
        String externalOrderId,
        String takeawayOrderId,
        String status,
        Instant processedAt
) {
}
