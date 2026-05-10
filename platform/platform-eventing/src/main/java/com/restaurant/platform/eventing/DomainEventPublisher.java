package com.restaurant.platform.eventing;

public interface DomainEventPublisher {

    void publish(EventEnvelope<?> eventEnvelope);
}
