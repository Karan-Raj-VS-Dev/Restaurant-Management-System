package com.restaurant.integration;

import com.restaurant.platform.eventing.AggregateTypes;
import com.restaurant.platform.eventing.DomainEventPublisher;
import com.restaurant.platform.eventing.EventEnvelopeFactory;
import com.restaurant.platform.eventing.EventKeys;
import com.restaurant.platform.eventing.contract.MarketplaceOrderReceivedEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class MarketplaceIntegrationService {

    private final EventEnvelopeFactory eventEnvelopeFactory;
    private final DomainEventPublisher domainEventPublisher;

    public MarketplaceIntegrationService(EventEnvelopeFactory eventEnvelopeFactory,
                                         DomainEventPublisher domainEventPublisher) {
        this.eventEnvelopeFactory = eventEnvelopeFactory;
        this.domainEventPublisher = domainEventPublisher;
    }

    public MarketplaceOrderResponse ingestOrder(String tenantId, String propertyId, MarketplaceOrderRequest request) {
        String integrationRequestId = "mp-" + UUID.randomUUID();
        MarketplaceOrderReceivedEvent payload = new MarketplaceOrderReceivedEvent(
                integrationRequestId,
                request.provider(),
                request.externalOrderId(),
                tenantId,
                propertyId,
                Instant.now()
        );

        domainEventPublisher.publish(eventEnvelopeFactory.create(
                EventKeys.MARKETPLACE_ORDER_RECEIVED,
                AggregateTypes.MARKETPLACE_REQUEST,
                integrationRequestId,
                propertyId,
                integrationRequestId,
                null,
                payload
        ));

        return new MarketplaceOrderResponse(
                integrationRequestId,
                tenantId,
                propertyId,
                request.provider(),
                request.externalOrderId(),
                null,
                "ACCEPTED_FOR_ASYNC_PROCESSING",
                Instant.now()
        );
    }
}
