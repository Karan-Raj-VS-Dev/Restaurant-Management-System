package com.restaurant.inventory.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.inventory.InventoryService;
import com.restaurant.platform.eventing.DomainEventHandler;
import com.restaurant.platform.eventing.EventEnvelope;
import com.restaurant.platform.eventing.EventEnvelopeParser;
import com.restaurant.platform.eventing.EventKeys;
import com.restaurant.platform.eventing.contract.OrderSubmittedToKitchenEvent;
import org.springframework.stereotype.Component;

@Component
public class InventoryOrderEventListener implements DomainEventHandler {

    private final ObjectMapper objectMapper;
    private final InventoryService inventoryService;

    public InventoryOrderEventListener(ObjectMapper objectMapper,
                                       InventoryService inventoryService) {
        this.objectMapper = objectMapper;
        this.inventoryService = inventoryService;
    }

    @Override
    public void handle(String rawMessage) {
        EventEnvelope<com.fasterxml.jackson.databind.JsonNode> envelope = EventEnvelopeParser.parse(rawMessage, objectMapper);
        if (!EventKeys.ORDER_SUBMITTED_TO_KITCHEN.equals(envelope.eventKey())) {
            return;
        }

        OrderSubmittedToKitchenEvent event = objectMapper.convertValue(envelope.payload(), OrderSubmittedToKitchenEvent.class);
        inventoryService.reserveForKitchenOrder(event, envelope.eventId());
    }
}
