package com.restaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.restaurant.platform.eventing.EventEnvelope;
import com.restaurant.platform.eventing.EventEnvelopeFactory;
import com.restaurant.platform.eventing.DomainEventPublisher;
import com.restaurant.platform.eventing.contract.MarketplaceOrderReceivedEvent;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarketplaceIntegrationServiceTest {

    @Mock
    private EventEnvelopeFactory eventEnvelopeFactory;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @Test
    void ingestOrderPublishesMarketplaceEventAndReturnsAcceptedResponse() {
        MarketplaceIntegrationService service = new MarketplaceIntegrationService(eventEnvelopeFactory, domainEventPublisher);
        MarketplaceOrderRequest request = new MarketplaceOrderRequest("Swiggy", "ext-001", "krusty-krab");
        EventEnvelope<MarketplaceOrderReceivedEvent> envelope = new EventEnvelope<>(
                "event-001",
                "marketplace.order.received",
                "marketplace-request",
                "mp-001",
                "krusty-krab",
                "marketplace-integration-service",
                Instant.parse("2026-06-15T07:10:00Z"),
                "corr-001",
                null,
                new MarketplaceOrderReceivedEvent("mp-001", "Swiggy", "ext-001", "bikini-bottom", "krusty-krab", Instant.parse("2026-06-15T07:10:00Z"))
        );
        when(eventEnvelopeFactory.create(any(), any(), any(), any(), any(), any(), any()))
                .thenAnswer(invocation -> envelope);

        MarketplaceOrderResponse response = service.ingestOrder("bikini-bottom", "krusty-krab", request);

        assertThat(response.tenantId()).isEqualTo("bikini-bottom");
        assertThat(response.propertyId()).isEqualTo("krusty-krab");
        assertThat(response.provider()).isEqualTo("Swiggy");
        assertThat(response.externalOrderId()).isEqualTo("ext-001");
        assertThat(response.status()).isEqualTo("ACCEPTED_FOR_ASYNC_PROCESSING");

        ArgumentCaptor<MarketplaceOrderReceivedEvent> eventCaptor = ArgumentCaptor.forClass(MarketplaceOrderReceivedEvent.class);
        verify(eventEnvelopeFactory).create(any(), any(), any(), any(), any(), any(), eventCaptor.capture());
        MarketplaceOrderReceivedEvent event = eventCaptor.getValue();
        assertThat(event.provider()).isEqualTo("Swiggy");
        assertThat(event.externalOrderId()).isEqualTo("ext-001");
        assertThat(event.tenantId()).isEqualTo("bikini-bottom");
        assertThat(event.propertyId()).isEqualTo("krusty-krab");

        verify(domainEventPublisher).publish(envelope);
    }
}
