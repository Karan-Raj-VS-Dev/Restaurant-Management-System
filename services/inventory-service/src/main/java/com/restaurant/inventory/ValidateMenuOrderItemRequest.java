package com.restaurant.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ValidateMenuOrderItemRequest(
        @NotBlank String itemId,
        @NotBlank String itemName,
        @Min(1) int quantity
) {
}
