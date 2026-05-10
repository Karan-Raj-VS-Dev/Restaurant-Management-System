package com.restaurant.platform.eventing.contract;

import java.time.Instant;

public record TakeawayOrderCreatedEvent(
        String takeawayOrderId,
        String tenantId,
        String propertyId,
        String channel,
        String sourceReferenceId,
        Instant occurredAt
) {
}
