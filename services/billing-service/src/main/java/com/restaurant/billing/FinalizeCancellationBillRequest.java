package com.restaurant.billing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record FinalizeCancellationBillRequest(
        @NotBlank String reason,
        @NotNull @PositiveOrZero BigDecimal cancellationFee
) {
}
