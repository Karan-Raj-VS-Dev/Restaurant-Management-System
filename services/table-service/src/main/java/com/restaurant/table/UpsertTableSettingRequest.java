package com.restaurant.table;

public record UpsertTableSettingRequest(
        String tableNumber,
        String displayName,
        String floorName,
        String sectionName,
        int capacity,
        String status,
        boolean active
) {
}
