package com.restaurant.platform.eventing.contract;

import java.time.Instant;

public record MarketplaceOrderReceivedEvent(
        String integrationRequestId,
        String provider,
        String externalOrderId,
        String tenantId,
        String propertyId,
        Instant occurredAt
) {
}
