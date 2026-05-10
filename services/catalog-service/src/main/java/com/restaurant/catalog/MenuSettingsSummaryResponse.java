package com.restaurant.catalog;

import java.util.List;

public record MenuSettingsSummaryResponse(
        int activeMenuItems,
        int recipeLinks,
        List<String> editableFields,
        List<MenuSettingsItemResponse> items
) {
}
