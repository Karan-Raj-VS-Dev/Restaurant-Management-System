package com.restaurant.billing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record BillLine(
        @NotBlank String itemId,
        @NotBlank String itemName,
        @Positive int quantity,
        @Positive BigDecimal unitPrice
) {
}
