package com.restaurant.billing;

import java.util.List;

public record BillingSettingsSummaryResponse(
        List<TaxSettingResponse> taxes,
        List<BillingTemplatePlaceholderResponse> templates
) {
}
