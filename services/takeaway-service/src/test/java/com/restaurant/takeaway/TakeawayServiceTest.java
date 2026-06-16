package com.restaurant.takeaway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.restaurant.platform.eventing.DomainEventPublisher;
import com.restaurant.platform.eventing.EventEnvelope;
import com.restaurant.platform.eventing.EventEnvelopeFactory;
import com.restaurant.platform.eventing.contract.MarketplaceOrderReceivedEvent;
import com.restaurant.platform.eventing.contract.TakeawayOrderCreatedEvent;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TakeawayServiceTest {

    @Mock
    private TakeawayOrderStore takeawayOrderStore;

    @Mock
    private DomainEventPublisher publisher;

    private TakeawayService service;

    @BeforeEach
    void setUp() {
        service = new TakeawayService(takeawayOrderStore, new EventEnvelopeFactory("test-suite"), publisher);
    }

    @Test
    void createTakeawayOrderCreatesAndPublishes() {
        CreateTakeawayOrderRequest request = new CreateTakeawayOrderRequest("krusty-krab", "DIRECT");
        TakeawayOrderRecord record = new TakeawayOrderRecord(
                "to-001",
                "bikini-bottom",
                "krusty-krab",
                "DIRECT",
                null,
                "CREATED",
                Instant.parse("2026-06-15T10:00:00Z")
        );
        when(takeawayOrderStore.createOrder("bikini-bottom", "krusty-krab", "DIRECT", null)).thenReturn(record);

        TakeawayOrderResponse response = service.createTakeawayOrder("bikini-bottom", "krusty-krab", request);

        assertThat(response.takeawayOrderId()).isEqualTo("to-001");
        assertThat(response.status()).isEqualTo(TakeawayStatus.CREATED);
        verify(takeawayOrderStore).createOrder("bikini-bottom", "krusty-krab", "DIRECT", null);
        ArgumentCaptor<EventEnvelope<?>> captor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(publisher).publish(captor.capture());
        assertThat(captor.getValue().causationId()).isNull();
        assertThat(captor.getValue().payload()).isInstanceOfSatisfying(TakeawayOrderCreatedEvent.class, event -> {
            assertThat(event.takeawayOrderId()).isEqualTo("to-001");
            assertThat(event.channel()).isEqualTo("DIRECT");
            assertThat(event.sourceReferenceId()).isNull();
            assertThat(event.tenantId()).isEqualTo("bikini-bottom");
            assertThat(event.propertyId()).isEqualTo("krusty-krab");
        });
    }

    @Test
    void getTakeawayOrderDelegatesToStore() {
        TakeawayOrderRecord record = new TakeawayOrderRecord(
                "to-002",
                "bikini-bottom",
                "krusty-krab",
                "DIRECT",
                null,
                "PREPARING",
                Instant.parse("2026-06-15T10:05:00Z")
        );
        when(takeawayOrderStore.find("bikini-bottom", "krusty-krab", "to-002")).thenReturn(record);

        TakeawayOrderResponse response = service.getTakeawayOrder("bikini-bottom", "krusty-krab", "to-002");

        assertThat(response.takeawayOrderId()).isEqualTo("to-002");
        assertThat(response.status()).isEqualTo(TakeawayStatus.PREPARING);
    }

    @Test
    void createOrderFromMarketplacePublishesCreatedEvent() {
        MarketplaceOrderReceivedEvent event = new MarketplaceOrderReceivedEvent(
                "int-001",
                "SWIGGY",
                "external-001",
                "bikini-bottom",
                "krusty-krab",
                Instant.parse("2026-06-15T10:10:00Z")
        );
        TakeawayOrderRecord record = new TakeawayOrderRecord(
                "to-003",
                "bikini-bottom",
                "krusty-krab",
                "SWIGGY",
                "int-001",
                "CREATED",
                Instant.parse("2026-06-15T10:10:00Z")
        );
        when(takeawayOrderStore.createOrder("bikini-bottom", "krusty-krab", "SWIGGY", "int-001")).thenReturn(record);

        service.createOrderFromMarketplace(event, "cause-001");

        verify(takeawayOrderStore).createOrder("bikini-bottom", "krusty-krab", "SWIGGY", "int-001");
        ArgumentCaptor<EventEnvelope<?>> captor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(publisher).publish(captor.capture());
        assertThat(captor.getValue().causationId()).isEqualTo("cause-001");
        assertThat(captor.getValue().payload()).isInstanceOfSatisfying(TakeawayOrderCreatedEvent.class, published -> {
            assertThat(published.takeawayOrderId()).isEqualTo("to-003");
            assertThat(published.channel()).isEqualTo("SWIGGY");
            assertThat(published.sourceReferenceId()).isEqualTo("int-001");
            assertThat(published.tenantId()).isEqualTo("bikini-bottom");
            assertThat(published.propertyId()).isEqualTo("krusty-krab");
        });
    }
}
