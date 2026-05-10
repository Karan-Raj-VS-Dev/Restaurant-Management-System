package com.restaurant.table;

import java.time.Instant;

public record UpdateTableStatusRequest(
        String targetStatus,
        Integer partySize,
        String waiterId,
        String cleanerId,
        Integer reservationPartySize,
        Instant reservationTime,
        Boolean immediate,
        Boolean overrideReservationWarning
) {
}
