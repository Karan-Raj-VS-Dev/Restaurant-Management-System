package com.restaurant.kitchen;

import com.restaurant.kitchen.persistence.entity.KitchenTicketEntity;
import com.restaurant.kitchen.persistence.entity.KitchenTicketItemEntity;
import com.restaurant.kitchen.persistence.repository.KitchenTicketItemRepository;
import com.restaurant.kitchen.persistence.repository.KitchenTicketRepository;
import com.restaurant.platform.eventing.AggregateTypes;
import com.restaurant.platform.eventing.DomainEventPublisher;
import com.restaurant.platform.eventing.EventEnvelopeFactory;
import com.restaurant.platform.eventing.EventKeys;
import com.restaurant.platform.eventing.contract.KitchenStatusUpdatedEvent;
import com.restaurant.platform.eventing.contract.KitchenTicketCreatedEvent;
import com.restaurant.platform.eventing.contract.OrderSubmittedToKitchenEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class KitchenService {

    private final KitchenTicketRepository kitchenTicketRepository;
    private final KitchenTicketItemRepository kitchenTicketItemRepository;
    private final EventEnvelopeFactory eventEnvelopeFactory;
    private final DomainEventPublisher domainEventPublisher;

    public KitchenService(KitchenTicketRepository kitchenTicketRepository,
                          KitchenTicketItemRepository kitchenTicketItemRepository,
                          EventEnvelopeFactory eventEnvelopeFactory,
                          DomainEventPublisher domainEventPublisher) {
        this.kitchenTicketRepository = kitchenTicketRepository;
        this.kitchenTicketItemRepository = kitchenTicketItemRepository;
        this.eventEnvelopeFactory = eventEnvelopeFactory;
        this.domainEventPublisher = domainEventPublisher;
    }

    public List<KitchenTicketResponse> listTickets(String tenantId, String propertyId) {
        return kitchenTicketRepository.findByTenantIdAndPropertyIdOrderByCreatedAtAsc(tenantId, propertyId).stream()
                .map(this::toResponse)
                .toList();
    }

    public KitchenTicketResponse createTicket(String tenantId, String propertyId, CreateKitchenTicketRequest request) {
        KitchenTicketEntity ticket = createTicketEntity(
                request.orderId(),
                tenantId,
                propertyId,
                request.cookId(),
                null
        );
        publishCreated(ticket, null, ticket.getOrderId());
        return toResponse(ticket);
    }

    public void createTicketForOrder(OrderSubmittedToKitchenEvent event, String causationId) {
        KitchenTicketEntity ticket = createTicketEntity(
                event.orderId(),
                event.tenantId(),
                event.propertyId(),
                null,
                event
        );
        publishCreated(ticket, causationId, event.orderId());
    }

    public KitchenTicketResponse acceptTicket(String tenantId, String propertyId, String ticketId, UpdateKitchenTicketRequest request) {
        KitchenTicketEntity ticket = updateStatus(
                tenantId,
                propertyId,
                ticketId,
                KitchenStatus.ACCEPTED,
                request == null ? null : request.cookId()
        );
        publishStatusUpdate(ticket);
        return toResponse(ticket);
    }

    public KitchenTicketResponse markReady(String tenantId, String propertyId, String ticketId, UpdateKitchenTicketRequest request) {
        KitchenTicketEntity ticket = updateStatus(
                tenantId,
                propertyId,
                ticketId,
                KitchenStatus.READY,
                request == null ? null : request.cookId()
        );
        publishStatusUpdate(ticket);
        return toResponse(ticket);
    }

    public KitchenTicketResponse markServed(String tenantId, String propertyId, String ticketId) {
        KitchenTicketEntity ticket = updateStatus(tenantId, propertyId, ticketId, KitchenStatus.SERVED, null);
        publishStatusUpdate(ticket);
        return toResponse(ticket);
    }

    private KitchenTicketEntity createTicketEntity(String orderId,
                                                   String tenantId,
                                                   String propertyId,
                                                   String cookId,
                                                   OrderSubmittedToKitchenEvent sourceEvent) {
        KitchenTicketEntity existing = kitchenTicketRepository.findByOrderId(orderId).orElse(null);
        if (existing != null) {
            if (cookId != null && !cookId.isBlank()) {
                existing.setAssignedCookId(cookId);
            }
            return kitchenTicketRepository.save(existing);
        }

        KitchenTicketEntity entity = KitchenTicketEntity.create(
                "ticket-" + UUID.randomUUID(),
                orderId,
                tenantId,
                propertyId,
                sourceEvent == null ? null : sourceEvent.tableId(),
                KitchenStatus.RECEIVED.name(),
                blankToNull(cookId),
                Instant.now()
        );
        KitchenTicketEntity saved = kitchenTicketRepository.save(entity);

        if (sourceEvent != null) {
            int index = 0;
            for (var item : sourceEvent.items()) {
                KitchenTicketItemEntity itemEntity = KitchenTicketItemEntity.create(
                        "ticket-item-" + UUID.randomUUID(),
                        saved.getTicketId(),
                        saved.getOrderId() + "-line-" + index++,
                        item.itemId(),
                        item.itemName(),
                        item.quantity(),
                        KitchenStatus.RECEIVED.name()
                );
                kitchenTicketItemRepository.save(itemEntity);
            }
        }

        return saved;
    }

    private KitchenTicketEntity updateStatus(String tenantId,
                                             String propertyId,
                                             String ticketId,
                                             KitchenStatus nextStatus,
                                             String cookId) {
        KitchenTicketEntity entity = kitchenTicketRepository.findById(ticketId)
                .filter(ticket -> tenantId.equals(ticket.getTenantId()) && propertyId.equals(ticket.getPropertyId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kitchen ticket was not found."));

        if (cookId != null && !cookId.isBlank()) {
            entity.setAssignedCookId(cookId);
        }
        entity.setTicketStatus(nextStatus.name());
        Instant now = Instant.now();
        if (nextStatus == KitchenStatus.ACCEPTED && entity.getAcceptedAt() == null) {
            entity.setAcceptedAt(now);
        }
        if (nextStatus == KitchenStatus.READY) {
            entity.setReadyAt(now);
        }
        if (nextStatus == KitchenStatus.SERVED) {
            entity.setCompletedAt(now);
        }
        KitchenTicketEntity saved = kitchenTicketRepository.save(entity);

        List<KitchenTicketItemEntity> items = kitchenTicketItemRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        String prepStatus = switch (nextStatus) {
            case RECEIVED -> KitchenStatus.RECEIVED.name();
            case ACCEPTED, PREPARING -> KitchenStatus.PREPARING.name();
            case READY -> KitchenStatus.READY.name();
            case SERVED -> KitchenStatus.SERVED.name();
        };
        items.forEach(item -> item.setPrepStatus(prepStatus));
        kitchenTicketItemRepository.saveAll(items);
        return saved;
    }

    private void publishCreated(KitchenTicketEntity ticket, String causationId, String partitionKey) {
        domainEventPublisher.publish(eventEnvelopeFactory.create(
                EventKeys.KITCHEN_TICKET_CREATED,
                AggregateTypes.KITCHEN_TICKET,
                ticket.getTicketId(),
                ticket.getPropertyId(),
                partitionKey,
                causationId,
                new KitchenTicketCreatedEvent(
                        ticket.getTicketId(),
                        ticket.getOrderId(),
                        ticket.getTenantId(),
                        ticket.getPropertyId(),
                        ticket.getAssignedCookId(),
                        ticket.getTicketStatus(),
                        ticket.getCreatedAt()
                )
        ));
    }

    private void publishStatusUpdate(KitchenTicketEntity ticket) {
        domainEventPublisher.publish(eventEnvelopeFactory.create(
                EventKeys.KITCHEN_STATUS_UPDATED,
                AggregateTypes.KITCHEN_TICKET,
                ticket.getTicketId(),
                ticket.getPropertyId(),
                ticket.getOrderId(),
                null,
                new KitchenStatusUpdatedEvent(
                        ticket.getTicketId(),
                        ticket.getOrderId(),
                        ticket.getTenantId(),
                        ticket.getPropertyId(),
                        ticket.getAssignedCookId(),
                        ticket.getTicketStatus(),
                        Instant.now()
                )
        ));
    }

    private KitchenTicketResponse toResponse(KitchenTicketEntity ticket) {
        Instant updatedAt = ticket.getCompletedAt() != null
                ? ticket.getCompletedAt()
                : ticket.getReadyAt() != null
                ? ticket.getReadyAt()
                : ticket.getAcceptedAt() != null
                ? ticket.getAcceptedAt()
                : ticket.getCreatedAt();
        return new KitchenTicketResponse(
                ticket.getTicketId(),
                ticket.getOrderId(),
                ticket.getPropertyId(),
                ticket.getAssignedCookId() == null ? "cook-pending" : ticket.getAssignedCookId(),
                KitchenStatus.valueOf(ticket.getTicketStatus()),
                updatedAt
        );
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
