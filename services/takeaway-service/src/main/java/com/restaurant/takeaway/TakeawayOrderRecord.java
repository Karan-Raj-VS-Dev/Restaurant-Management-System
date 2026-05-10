package com.restaurant.takeaway;

import java.time.Instant;

public record TakeawayOrderRecord(
        String takeawayOrderId,
        String tenantId,
        String propertyId,
        String channel,
        String sourceReferenceId,
        String status,
        Instant updatedAt
) {
}
