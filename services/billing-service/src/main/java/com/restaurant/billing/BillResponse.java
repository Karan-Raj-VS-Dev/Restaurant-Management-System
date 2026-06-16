package com.restaurant.billing;

import java.math.BigDecimal;
import java.util.List;

public record BillResponse(
        String billId,
        String lastOrderId,
        List<String> orderIds,
        String tableId,
        String sessionId,
        String customerId,
        BillStatus status,
        BillSettlementType settlementType,
        String cancellationReason,
        BigDecimal cancellationFee,
        List<BillLine> items,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal total
) {
}
