package com.restaurant.takeaway;

import com.restaurant.platform.eventing.AggregateTypes;
import com.restaurant.platform.eventing.DomainEventPublisher;
import com.restaurant.platform.eventing.EventEnvelopeFactory;
import com.restaurant.platform.eventing.EventKeys;
import com.restaurant.platform.eventing.contract.MarketplaceOrderReceivedEvent;
import com.restaurant.platform.eventing.contract.TakeawayOrderCreatedEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TakeawayService {

    private final TakeawayOrderStore takeawayOrderStore;
    private final EventEnvelopeFactory eventEnvelopeFactory;
    private final DomainEventPublisher domainEventPublisher;

    public TakeawayService(TakeawayOrderStore takeawayOrderStore,
                           EventEnvelopeFactory eventEnvelopeFactory,
                           DomainEventPublisher domainEventPublisher) {
        this.takeawayOrderStore = takeawayOrderStore;
        this.eventEnvelopeFactory = eventEnvelopeFactory;
        this.domainEventPublisher = domainEventPublisher;
    }

    public TakeawayOrderResponse createTakeawayOrder(String tenantId, String propertyId, CreateTakeawayOrderRequest request) {
        TakeawayOrderRecord record = takeawayOrderStore.createOrder(
                tenantId,
                propertyId,
                request.channel(),
                null
        );
        publishCreated(record, null);
        return toResponse(record);
    }

    public TakeawayOrderResponse getTakeawayOrder(String tenantId, String propertyId, String takeawayOrderId) {
        return toResponse(takeawayOrderStore.find(tenantId, propertyId, takeawayOrderId));
    }

    public void createOrderFromMarketplace(MarketplaceOrderReceivedEvent event, String causationId) {
        TakeawayOrderRecord record = takeawayOrderStore.createOrder(
                event.tenantId(),
                event.propertyId(),
                event.provider(),
                event.integrationRequestId()
        );
        publishCreated(record, causationId);
    }

    public void publishCreated(TakeawayOrderRecord record, String causationId) {
        domainEventPublisher.publish(eventEnvelopeFactory.create(
                EventKeys.TAKEAWAY_ORDER_CREATED,
                AggregateTypes.TAKEAWAY_ORDER,
                record.takeawayOrderId(),
                record.propertyId(),
                record.takeawayOrderId(),
                causationId,
                new TakeawayOrderCreatedEvent(
                        record.takeawayOrderId(),
                        record.tenantId(),
                        record.propertyId(),
                        record.channel(),
                        record.sourceReferenceId(),
                        Instant.now()
                )
        ));
    }

    private TakeawayOrderResponse toResponse(TakeawayOrderRecord record) {
        return new TakeawayOrderResponse(
                record.takeawayOrderId(),
                record.tenantId(),
                record.propertyId(),
                record.channel(),
                TakeawayStatus.valueOf(record.status()),
                record.updatedAt()
        );
    }
}
