package com.restaurant.kitchen;

import java.time.Instant;

public record KitchenTicketResponse(
        String ticketId,
        String orderId,
        String propertyId,
        String cookId,
        KitchenStatus status,
        Instant updatedAt
) {
}
