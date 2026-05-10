package com.restaurant.kitchen;

import com.restaurant.platform.eventing.AggregateTypes;
import com.restaurant.platform.eventing.DomainEventPublisher;
import com.restaurant.platform.eventing.EventEnvelopeFactory;
import com.restaurant.platform.eventing.EventKeys;
import com.restaurant.platform.eventing.contract.KitchenStatusUpdatedEvent;
import com.restaurant.platform.eventing.contract.KitchenTicketCreatedEvent;
import com.restaurant.platform.eventing.contract.OrderSubmittedToKitchenEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class KitchenService {

    private final KitchenTicketStore kitchenTicketStore;
    private final EventEnvelopeFactory eventEnvelopeFactory;
    private final DomainEventPublisher domainEventPublisher;

    public KitchenService(KitchenTicketStore kitchenTicketStore,
                          EventEnvelopeFactory eventEnvelopeFactory,
                          DomainEventPublisher domainEventPublisher) {
        this.kitchenTicketStore = kitchenTicketStore;
        this.eventEnvelopeFactory = eventEnvelopeFactory;
        this.domainEventPublisher = domainEventPublisher;
    }

    public List<KitchenTicketResponse> listTickets(String tenantId, String propertyId) {
        return kitchenTicketStore.listTickets(tenantId, propertyId).stream()
                .map(this::toResponse)
                .toList();
    }

    public KitchenTicketResponse createTicket(String tenantId, String propertyId, CreateKitchenTicketRequest request) {
        KitchenTicketRecord ticket = kitchenTicketStore.createTicket(
                request.orderId(),
                tenantId,
                propertyId,
                request.cookId()
        );
        publishCreated(ticket, null, ticket.orderId());
        return toResponse(ticket);
    }

    public void createTicketForOrder(OrderSubmittedToKitchenEvent event, String causationId) {
        KitchenTicketRecord ticket = kitchenTicketStore.createTicket(
                event.orderId(),
                event.tenantId(),
                event.propertyId(),
                "cook-auto-001"
        );
        publishCreated(ticket, causationId, event.orderId());
    }

    public KitchenTicketResponse acceptTicket(String tenantId, String propertyId, String ticketId) {
        KitchenTicketRecord ticket = kitchenTicketStore.updateStatus(tenantId, propertyId, ticketId, "ACCEPTED");
        publishStatusUpdate(ticket);
        return toResponse(ticket);
    }

    public KitchenTicketResponse markReady(String tenantId, String propertyId, String ticketId) {
        KitchenTicketRecord ticket = kitchenTicketStore.updateStatus(tenantId, propertyId, ticketId, "READY");
        publishStatusUpdate(ticket);
        return toResponse(ticket);
    }

    private void publishCreated(KitchenTicketRecord ticket, String causationId, String partitionKey) {
        domainEventPublisher.publish(eventEnvelopeFactory.create(
                EventKeys.KITCHEN_TICKET_CREATED,
                AggregateTypes.KITCHEN_TICKET,
                ticket.ticketId(),
                ticket.propertyId(),
                partitionKey,
                causationId,
                new KitchenTicketCreatedEvent(
                        ticket.ticketId(),
                        ticket.orderId(),
                        ticket.tenantId(),
                        ticket.propertyId(),
                        ticket.cookId(),
                        ticket.status(),
                        ticket.updatedAt()
                )
        ));
    }

    private void publishStatusUpdate(KitchenTicketRecord ticket) {
        domainEventPublisher.publish(eventEnvelopeFactory.create(
                EventKeys.KITCHEN_STATUS_UPDATED,
                AggregateTypes.KITCHEN_TICKET,
                ticket.ticketId(),
                ticket.propertyId(),
                ticket.orderId(),
                null,
                new KitchenStatusUpdatedEvent(
                        ticket.ticketId(),
                        ticket.orderId(),
                        ticket.tenantId(),
                        ticket.propertyId(),
                        ticket.cookId(),
                        ticket.status(),
                        Instant.now()
                )
        ));
    }

    private KitchenTicketResponse toResponse(KitchenTicketRecord ticket) {
        return new KitchenTicketResponse(
                ticket.ticketId(),
                ticket.orderId(),
                ticket.propertyId(),
                ticket.cookId(),
                KitchenStatus.valueOf(ticket.status()),
                ticket.updatedAt()
        );
    }
}
