package com.restaurant.billing.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.billing.BillingService;
import com.restaurant.platform.eventing.DomainEventHandler;
import com.restaurant.platform.eventing.EventEnvelope;
import com.restaurant.platform.eventing.EventEnvelopeParser;
import com.restaurant.platform.eventing.EventKeys;
import com.restaurant.platform.eventing.contract.OrderCreatedEvent;
import org.springframework.stereotype.Component;

@Component
public class BillingOrderEventListener implements DomainEventHandler {

    private final ObjectMapper objectMapper;
    private final BillingService billingService;

    public BillingOrderEventListener(ObjectMapper objectMapper,
                                     BillingService billingService) {
        this.objectMapper = objectMapper;
        this.billingService = billingService;
    }

    @Override
    public void handle(String rawMessage) {
        EventEnvelope<com.fasterxml.jackson.databind.JsonNode> envelope = EventEnvelopeParser.parse(rawMessage, objectMapper);
        if (!EventKeys.ORDER_CREATED.equals(envelope.eventKey())) {
            return;
        }

        OrderCreatedEvent event = objectMapper.convertValue(envelope.payload(), OrderCreatedEvent.class);
        billingService.draftBillFromOrder(event, envelope.eventId());
    }
}
