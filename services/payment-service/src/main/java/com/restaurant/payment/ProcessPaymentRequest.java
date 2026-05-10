package com.restaurant.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProcessPaymentRequest(
        @NotBlank String billId,
        @NotNull PaymentMethod method,
        @Positive BigDecimal amount
) {
}
