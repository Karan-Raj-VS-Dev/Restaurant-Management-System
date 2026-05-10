package com.restaurant.billing;

import java.util.Map;

public record BillingTemplateRequest(
        String templateId,
        String templateName,
        Map<String, Object> description,
        String status
) {
}
