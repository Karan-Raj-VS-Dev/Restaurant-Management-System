package com.restaurant.property;

import java.util.List;

public record ImportWorkbookSheetResponse(
        String sheetName,
        String purpose,
        List<String> requiredColumns
) {
}
