package com.restaurant.property;

import java.util.List;

public record AreaSectionSettingResponse(
        String areaSectionId,
        String floorName,
        String sectionName,
        int maxTableCount,
        List<String> waiterNames,
        List<String> cleanerNames,
        String status
) {
}
