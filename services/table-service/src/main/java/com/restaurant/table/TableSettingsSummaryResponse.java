package com.restaurant.table;

import java.util.List;

public record TableSettingsSummaryResponse(
        int configuredTables,
        List<String> editableFields,
        List<TableSettingRecordResponse> tables
) {
}
