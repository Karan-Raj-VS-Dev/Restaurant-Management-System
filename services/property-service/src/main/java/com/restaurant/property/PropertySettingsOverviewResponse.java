package com.restaurant.property;

import java.util.List;

public record PropertySettingsOverviewResponse(
        String tenantId,
        String propertyId,
        List<PropertySettingsModuleResponse> modules,
        List<ImportWorkbookSheetResponse> importWorkbookSheets
) {
}
