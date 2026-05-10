package com.restaurant.platform.eventing.contract;

import java.time.Instant;

public record TableAssignedEvent(
        String tableId,
        String tenantId,
        String propertyId,
        String waiterId,
        int capacity,
        String status,
        Instant occurredAt
) {
}
