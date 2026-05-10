package com.restaurant.billing;

import java.math.BigDecimal;

public record TaxSettingResponse(
        String taxId,
        String taxName,
        BigDecimal ratePercent,
        String status,
        String appliesTo
) {
}
