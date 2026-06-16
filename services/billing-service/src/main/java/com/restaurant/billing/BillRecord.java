package com.restaurant.billing;

import java.math.BigDecimal;
import java.util.List;

public record BillRecord(
        String billId,
        String lastOrderId,
        List<String> orderIds,
        String tenantId,
        String propertyId,
        String tableId,
        String sessionId,
        String customerId,
        String status,
        String settlementType,
        String cancellationReason,
        BigDecimal cancellationFee,
        List<BillLineRecord> items,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal total
) {
}
