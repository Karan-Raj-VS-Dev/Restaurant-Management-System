package com.restaurant.kitchen;

public record UpdateKitchenTicketRequest(
        String cookId,
        String reason,
        String reuseTicketId
) {
}
