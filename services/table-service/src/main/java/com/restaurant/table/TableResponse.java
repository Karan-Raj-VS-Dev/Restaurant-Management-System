package com.restaurant.table;

public record TableResponse(
        String tableId,
        String tableNumber,
        String displayName,
        String propertyId,
        String floorName,
        String sectionName,
        int capacity,
        TableStatus status,
        String waiterId,
        String cleanerId,
        Integer currentPartySize,
        Integer reservationPartySize,
        java.time.Instant reservationTime,
        TableStatus pendingStatus,
        java.time.Instant pendingStatusAt
) {
}
