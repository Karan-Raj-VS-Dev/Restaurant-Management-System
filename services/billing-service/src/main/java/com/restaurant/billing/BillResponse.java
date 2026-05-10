package com.restaurant.billing;

import java.math.BigDecimal;
import java.util.List;

public record BillResponse(
        String billId,
        String orderId,
        String tableId,
        BillStatus status,
        List<BillLine> items,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal total
) {
}
