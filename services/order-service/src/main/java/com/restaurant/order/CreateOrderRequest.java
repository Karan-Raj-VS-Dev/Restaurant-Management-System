package com.restaurant.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank String propertyId,
        @NotBlank String tableId,
        String sessionId,
        @NotBlank String waiterId,
        String customerId,
        @NotEmpty List<OrderItem> items
) {
}
