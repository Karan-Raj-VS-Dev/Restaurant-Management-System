package com.restaurant.platform.eventing;

import java.time.Instant;

public record EventEnvelope<T>(
        String eventId,
        String eventKey,
        String aggregateType,
        String aggregateId,
        String partitionKey,
        String producer,
        Instant occurredAt,
        String correlationId,
        String causationId,
        T payload
) {
}
