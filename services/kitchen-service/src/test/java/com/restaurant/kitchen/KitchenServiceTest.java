package com.restaurant.kitchen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.restaurant.kitchen.persistence.entity.KitchenTicketEntity;
import com.restaurant.kitchen.persistence.entity.KitchenTicketItemEntity;
import com.restaurant.kitchen.persistence.repository.KitchenTicketItemRepository;
import com.restaurant.kitchen.persistence.repository.KitchenTicketRepository;
import com.restaurant.platform.eventing.DomainEventPublisher;
import com.restaurant.platform.eventing.EventEnvelopeFactory;
import com.restaurant.platform.eventing.contract.OrderLineItem;
import com.restaurant.platform.eventing.contract.OrderSubmittedToKitchenEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KitchenServiceTest {

    @Mock
    private KitchenTicketRepository kitchenTicketRepository;

    @Mock
    private KitchenTicketItemRepository kitchenTicketItemRepository;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    private final Map<String, KitchenTicketEntity> ticketsById = new LinkedHashMap<>();
    private final Map<String, List<KitchenTicketItemEntity>> itemsByTicketId = new LinkedHashMap<>();

    private KitchenService kitchenService;

    @BeforeEach
    void setUp() {
        kitchenService = new KitchenService(
                kitchenTicketRepository,
                kitchenTicketItemRepository,
                new EventEnvelopeFactory("test-suite"),
                domainEventPublisher
        );

        when(kitchenTicketRepository.save(any(KitchenTicketEntity.class))).thenAnswer(invocation -> {
            KitchenTicketEntity entity = invocation.getArgument(0);
            ticketsById.put(entity.getTicketId(), entity);
            return entity;
        });
        lenient().when(kitchenTicketRepository.findById(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(ticketsById.get(invocation.getArgument(0))));
        when(kitchenTicketRepository.findByOrderId(anyString()))
                .thenAnswer(invocation -> ticketsById.values().stream()
                        .filter(ticket -> invocation.getArgument(0).equals(ticket.getOrderId()))
                        .findFirst());
        lenient().when(kitchenTicketRepository.findByTenantIdAndPropertyIdOrderByCreatedAtAsc(anyString(), anyString()))
                .thenAnswer(invocation -> ticketsById.values().stream()
                        .filter(ticket -> invocation.getArgument(0).equals(ticket.getTenantId()) && invocation.getArgument(1).equals(ticket.getPropertyId()))
                        .toList());

        when(kitchenTicketItemRepository.save(any(KitchenTicketItemEntity.class))).thenAnswer(invocation -> {
            KitchenTicketItemEntity entity = invocation.getArgument(0);
            itemsByTicketId.computeIfAbsent(entity.getTicketId(), ignored -> new ArrayList<>()).add(entity);
            return entity;
        });
        lenient().when(kitchenTicketItemRepository.findByTicketIdOrderByOrderItemIdAsc(anyString()))
                .thenAnswer(invocation -> new ArrayList<>(itemsByTicketId.getOrDefault(invocation.getArgument(0), List.of())));
        lenient().when(kitchenTicketItemRepository.saveAll(anyCollection())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createTicketForOrderCreatesTicketItemsAndPublishesCreatedEvent() {
        kitchenService.createTicketForOrder(new OrderSubmittedToKitchenEvent(
                "order-001",
                "bikini-bottom",
                "krusty-krab",
                "table-01",
                "emp-001",
                List.of(
                        new OrderLineItem("item-001", "Margherita Pizza", 2),
                        new OrderLineItem("item-002", "Pasta Alfredo", 1)
                ),
                Instant.parse("2026-06-15T10:00:00Z")
        ), "cause-001");

        assertThat(ticketsById.values()).singleElement()
                .extracting(KitchenTicketEntity::getOrderId, KitchenTicketEntity::getTicketStatus)
                .containsExactly("order-001", KitchenStatus.RECEIVED.name());
        assertThat(itemsByTicketId.values()).singleElement()
                .satisfies(items -> assertThat(items).hasSize(2));
        verify(domainEventPublisher).publish(any());
    }

    @Test
    void acceptAndReadyUpdateTicketAndItemStatuses() {
        kitchenService.createTicketForOrder(new OrderSubmittedToKitchenEvent(
                "order-001",
                "bikini-bottom",
                "krusty-krab",
                "table-01",
                "emp-001",
                List.of(new OrderLineItem("item-001", "Margherita Pizza", 1)),
                Instant.parse("2026-06-15T10:00:00Z")
        ), "cause-001");

        String ticketId = ticketsById.keySet().iterator().next();
        KitchenTicketResponse accepted = kitchenService.acceptTicket(
                "bikini-bottom",
                "krusty-krab",
                ticketId,
                new UpdateKitchenTicketRequest("cook-001", null, null)
        );
        KitchenTicketResponse ready = kitchenService.markReady(
                "bikini-bottom",
                "krusty-krab",
                ticketId,
                new UpdateKitchenTicketRequest("cook-001", null, null)
        );

        assertThat(accepted.status()).isEqualTo(KitchenStatus.ACCEPTED);
        assertThat(ready.status()).isEqualTo(KitchenStatus.READY);
        assertThat(itemsByTicketId.get(ticketId)).extracting(KitchenTicketItemEntity::getPrepStatus)
                .containsExactly(KitchenStatus.READY.name());
    }

    @Test
    void markCancelledPersistsReasonAndCompletedStatus() {
        kitchenService.createTicketForOrder(new OrderSubmittedToKitchenEvent(
                "order-001",
                "bikini-bottom",
                "krusty-krab",
                "table-01",
                "emp-001",
                List.of(new OrderLineItem("item-001", "Margherita Pizza", 1)),
                Instant.parse("2026-06-15T10:00:00Z")
        ), "cause-001");
        String ticketId = ticketsById.keySet().iterator().next();

        KitchenTicketResponse response = kitchenService.markCancelled(
                "bikini-bottom",
                "krusty-krab",
                ticketId,
                new UpdateKitchenTicketRequest("cook-001", "Customer left", null)
        );

        assertThat(response.status()).isEqualTo(KitchenStatus.CANCELLED);
        assertThat(response.cancellationReason()).isEqualTo("Customer left");
        assertThat(itemsByTicketId.get(ticketId)).extracting(KitchenTicketItemEntity::getPrepStatus)
                .containsExactly(KitchenStatus.CANCELLED.name());
    }
}
