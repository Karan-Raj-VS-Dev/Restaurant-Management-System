package com.restaurant.audit;

import java.time.Instant;

public record AuditEntry(
        String eventId,
        String referenceId,
        String tenantId,
        String propertyId,
        String eventKey,
        String message,
        String producer,
        Instant occurredAt
) {
}
