package com.restaurant.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record OrderItem(
        @NotBlank String itemId,
        @NotBlank String itemName,
        @Positive int quantity,
        BigDecimal unitPrice
) {
}
