package com.restaurant.property;

import java.util.List;

public record AreaSectionSettingsSummaryResponse(
        List<String> editableFields,
        List<AreaSectionSettingResponse> records
) {
}
