package com.restaurant.audit;

import java.time.Instant;

public record AuditEventResponse(
        String referenceId,
        String tenantId,
        String propertyId,
        String eventType,
        String message,
        Instant occurredAt
) {
}
