package com.restaurant.billing;

import java.math.BigDecimal;
import java.util.List;

public record BillRecord(
        String billId,
        String orderId,
        List<String> orderIds,
        String tenantId,
        String propertyId,
        String tableId,
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
