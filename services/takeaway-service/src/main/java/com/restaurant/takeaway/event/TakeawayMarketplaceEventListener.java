package com.restaurant.takeaway.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.platform.eventing.DomainEventHandler;
import com.restaurant.platform.eventing.EventEnvelope;
import com.restaurant.platform.eventing.EventEnvelopeParser;
import com.restaurant.platform.eventing.EventKeys;
import com.restaurant.platform.eventing.contract.MarketplaceOrderReceivedEvent;
import com.restaurant.takeaway.TakeawayService;
import org.springframework.stereotype.Component;

@Component
public class TakeawayMarketplaceEventListener implements DomainEventHandler {

    private final ObjectMapper objectMapper;
    private final TakeawayService takeawayService;

    public TakeawayMarketplaceEventListener(ObjectMapper objectMapper,
                                            TakeawayService takeawayService) {
        this.objectMapper = objectMapper;
        this.takeawayService = takeawayService;
    }

    @Override
    public void handle(String rawMessage) {
        EventEnvelope<com.fasterxml.jackson.databind.JsonNode> envelope = EventEnvelopeParser.parse(rawMessage, objectMapper);
        if (!EventKeys.MARKETPLACE_ORDER_RECEIVED.equals(envelope.eventKey())) {
            return;
        }

        MarketplaceOrderReceivedEvent event = objectMapper.convertValue(envelope.payload(), MarketplaceOrderReceivedEvent.class);
        takeawayService.createOrderFromMarketplace(event, envelope.eventId());
    }
}
