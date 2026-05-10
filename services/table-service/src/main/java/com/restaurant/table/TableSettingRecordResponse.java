package com.restaurant.table;

public record TableSettingRecordResponse(
        String tableId,
        String tableNumber,
        String displayName,
        String floorName,
        String sectionName,
        int capacity,
        String status,
        boolean active
) {
}
