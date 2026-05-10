package com.restaurant.insights.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.insights.OperationsInsightsService;
import com.restaurant.platform.eventing.DomainEventHandler;
import com.restaurant.platform.eventing.EventEnvelope;
import com.restaurant.platform.eventing.EventEnvelopeParser;
import com.restaurant.platform.eventing.EventKeys;
import com.restaurant.platform.eventing.contract.OrderCreatedEvent;
import com.restaurant.platform.eventing.contract.PaymentSucceededEvent;
import com.restaurant.platform.eventing.contract.StockAlertEvent;
import org.springframework.stereotype.Component;

@Component
public class OperationsInsightsEventListener implements DomainEventHandler {

    private final ObjectMapper objectMapper;
    private final OperationsInsightsService operationsInsightsService;

    public OperationsInsightsEventListener(ObjectMapper objectMapper,
                                           OperationsInsightsService operationsInsightsService) {
        this.objectMapper = objectMapper;
        this.operationsInsightsService = operationsInsightsService;
    }

    @Override
    public void handle(String rawMessage) {
        EventEnvelope<com.fasterxml.jackson.databind.JsonNode> envelope = EventEnvelopeParser.parse(rawMessage, objectMapper);

        if (EventKeys.ORDER_CREATED.equals(envelope.eventKey())) {
            OrderCreatedEvent event = objectMapper.convertValue(envelope.payload(), OrderCreatedEvent.class);
            operationsInsightsService.recordOrder(event);
            return;
        }

        if (EventKeys.PAYMENT_SUCCEEDED.equals(envelope.eventKey())) {
            PaymentSucceededEvent event = objectMapper.convertValue(envelope.payload(), PaymentSucceededEvent.class);
            operationsInsightsService.recordSale(event);
            return;
        }

        if (EventKeys.STOCK_LOW.equals(envelope.eventKey()) || EventKeys.STOCK_OUT.equals(envelope.eventKey())) {
            StockAlertEvent event = objectMapper.convertValue(envelope.payload(), StockAlertEvent.class);
            operationsInsightsService.updateStock(event);
        }
    }
}
