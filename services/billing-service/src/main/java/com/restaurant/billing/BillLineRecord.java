package com.restaurant.billing;

import java.math.BigDecimal;

public record BillLineRecord(
        String itemId,
        String itemName,
        int quantity,
        BigDecimal unitPrice
) {
}
