package com.restaurant.platform.eventing;

import java.time.Instant;
import java.util.UUID;

public class EventEnvelopeFactory {

    private final String producer;

    public EventEnvelopeFactory(String producer) {
        this.producer = producer;
    }

    public <T> EventEnvelope<T> create(String eventKey,
                                       String aggregateType,
                                       String aggregateId,
                                       String partitionKey,
                                       String correlationId,
                                       String causationId,
                                       T payload) {
        return new EventEnvelope<>(
                UUID.randomUUID().toString(),
                eventKey,
                aggregateType,
                aggregateId,
                partitionKey,
                producer,
                Instant.now(),
                correlationId != null && !correlationId.isBlank() ? correlationId : aggregateId,
                causationId,
                payload
        );
    }
}
