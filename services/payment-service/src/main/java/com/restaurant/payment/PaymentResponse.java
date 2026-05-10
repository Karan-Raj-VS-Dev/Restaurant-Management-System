package com.restaurant.payment;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String paymentId,
        String billId,
        PaymentMethod method,
        PaymentStatus status,
        BigDecimal amount,
        Instant paidAt
) {
}
