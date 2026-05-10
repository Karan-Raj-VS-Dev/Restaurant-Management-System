package com.restaurant.platform.eventing.contract;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentSucceededEvent(
        String paymentId,
        String billId,
        String tenantId,
        String propertyId,
        BigDecimal amount,
        String method,
        Instant paidAt
) {
}
