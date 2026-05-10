package com.restaurant.billing;

import java.math.BigDecimal;

public record TaxSettingRequest(
        String taxId,
        String taxName,
        BigDecimal ratePercent,
        String appliesTo,
        String status
) {
}
