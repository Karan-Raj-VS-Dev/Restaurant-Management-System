package com.restaurant.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record OrderItem(
        @NotBlank String itemId,
        @NotBlank String itemName,
        @Positive int quantity
) {
}
