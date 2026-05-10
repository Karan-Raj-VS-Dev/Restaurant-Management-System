package com.restaurant.inventory;

public record ReservationResponse(
        String reservationId,
        String referenceId,
        boolean success,
        String message
) {
}
