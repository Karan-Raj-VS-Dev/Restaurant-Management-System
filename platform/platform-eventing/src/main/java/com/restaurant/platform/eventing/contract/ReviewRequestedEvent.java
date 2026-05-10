package com.restaurant.platform.eventing.contract;

import java.time.Instant;

public record ReviewRequestedEvent(
        String reviewRequestId,
        String billId,
        String tenantId,
        String propertyId,
        String customerId,
        Instant requestedAt
) {
}
