package com.restaurant.billing;

import java.math.BigDecimal;
import java.util.List;

public record BillRecord(
        String billId,
        String orderId,
        String tenantId,
        String propertyId,
        String tableId,
        String status,
        List<BillLineRecord> items,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal total
) {
}
