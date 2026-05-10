package com.restaurant.property;

import java.util.List;

public record UpsertAreaSectionSettingRequest(
        String floorName,
        String sectionName,
        int maxTableCount,
        List<String> waiterNames,
        List<String> cleanerNames,
        String status
) {
}
