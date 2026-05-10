package com.restaurant.kitchen;

import java.time.Instant;

public record KitchenTicketRecord(
        String ticketId,
        String orderId,
        String tenantId,
        String propertyId,
        String cookId,
        String status,
        Instant updatedAt
) {
}
