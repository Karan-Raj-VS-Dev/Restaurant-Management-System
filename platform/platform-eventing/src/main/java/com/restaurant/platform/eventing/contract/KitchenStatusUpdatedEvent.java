package com.restaurant.platform.eventing.contract;

import java.time.Instant;

public record KitchenStatusUpdatedEvent(
        String ticketId,
        String orderId,
        String tenantId,
        String propertyId,
        String cookId,
        String status,
        Instant occurredAt
) {
}
