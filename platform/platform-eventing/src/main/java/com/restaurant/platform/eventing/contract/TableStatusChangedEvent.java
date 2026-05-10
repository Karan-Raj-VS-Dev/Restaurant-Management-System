package com.restaurant.platform.eventing.contract;

import java.time.Instant;

public record TableStatusChangedEvent(
        String tableId,
        String tenantId,
        String propertyId,
        String status,
        Instant occurredAt
) {
}
