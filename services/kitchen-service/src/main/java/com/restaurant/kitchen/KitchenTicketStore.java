package com.restaurant.kitchen;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class KitchenTicketStore {

    private final Map<String, KitchenTicketRecord> tickets = new ConcurrentHashMap<>();

    public List<KitchenTicketRecord> listTickets(String tenantId, String propertyId) {
        return tickets.values().stream()
                .filter(ticket -> tenantId.equals(ticket.tenantId()) && propertyId.equals(ticket.propertyId()))
                .sorted((left, right) -> right.updatedAt().compareTo(left.updatedAt()))
                .toList();
    }

    public KitchenTicketRecord createTicket(String orderId, String tenantId, String propertyId, String cookId) {
        KitchenTicketRecord record = new KitchenTicketRecord(
                "ticket-" + UUID.randomUUID(),
                orderId,
                tenantId,
                propertyId,
                cookId,
                "RECEIVED",
                Instant.now()
        );
        tickets.put(record.ticketId(), record);
        return record;
    }

    public KitchenTicketRecord updateStatus(String tenantId, String propertyId, String ticketId, String status) {
        return tickets.compute(ticketId, (key, existing) -> {
            if (existing == null) {
                return new KitchenTicketRecord(ticketId, "order-unknown", tenantId, propertyId, "cook-unassigned", status, Instant.now());
            }
            return new KitchenTicketRecord(
                    existing.ticketId(),
                    existing.orderId(),
                    existing.tenantId(),
                    existing.propertyId(),
                    existing.cookId(),
                    status,
                    Instant.now()
            );
        });
    }

}
