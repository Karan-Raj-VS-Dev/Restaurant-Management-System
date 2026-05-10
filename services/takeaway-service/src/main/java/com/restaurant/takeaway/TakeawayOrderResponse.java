package com.restaurant.takeaway;

import java.time.Instant;

public record TakeawayOrderResponse(
        String takeawayOrderId,
        String tenantId,
        String propertyId,
        String channel,
        TakeawayStatus status,
        Instant updatedAt
) {
}
