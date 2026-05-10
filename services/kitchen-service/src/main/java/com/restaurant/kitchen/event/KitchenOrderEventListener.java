package com.restaurant.kitchen.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.kitchen.KitchenService;
import com.restaurant.platform.eventing.DomainEventHandler;
import com.restaurant.platform.eventing.EventEnvelope;
import com.restaurant.platform.eventing.EventEnvelopeParser;
import com.restaurant.platform.eventing.EventKeys;
import com.restaurant.platform.eventing.contract.OrderSubmittedToKitchenEvent;
import org.springframework.stereotype.Component;

@Component
public class KitchenOrderEventListener implements DomainEventHandler {

    private final ObjectMapper objectMapper;
    private final KitchenService kitchenService;

    public KitchenOrderEventListener(ObjectMapper objectMapper,
                                     KitchenService kitchenService) {
        this.objectMapper = objectMapper;
        this.kitchenService = kitchenService;
    }

    @Override
    public void handle(String rawMessage) {
        EventEnvelope<com.fasterxml.jackson.databind.JsonNode> envelope = EventEnvelopeParser.parse(rawMessage, objectMapper);
        if (!EventKeys.ORDER_SUBMITTED_TO_KITCHEN.equals(envelope.eventKey())) {
            return;
        }

        OrderSubmittedToKitchenEvent event = objectMapper.convertValue(envelope.payload(), OrderSubmittedToKitchenEvent.class);
        kitchenService.createTicketForOrder(event, envelope.eventId());
    }
}
