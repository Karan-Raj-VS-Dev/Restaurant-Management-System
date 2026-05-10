package com.restaurant.platform.eventing;

public interface DomainEventHandler {

    void handle(String rawMessage);
}
