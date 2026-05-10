package com.restaurant.kitchen;

import jakarta.validation.constraints.NotBlank;

public record CreateKitchenTicketRequest(
        @NotBlank String orderId,
        @NotBlank String propertyId,
        @NotBlank String cookId
) {
}
