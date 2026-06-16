package com.restaurant.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.restaurant.order.persistence.entity.OrderEntity;
import com.restaurant.order.persistence.entity.OrderItemEntity;
import com.restaurant.order.persistence.entity.OrderStatusHistoryEntity;
import com.restaurant.order.persistence.repository.OrderItemRepository;
import com.restaurant.order.persistence.repository.OrderRepository;
import com.restaurant.order.persistence.repository.OrderStatusHistoryRepository;
import com.restaurant.platform.eventing.DomainEventPublisher;
import com.restaurant.platform.eventing.EventEnvelope;
import com.restaurant.platform.eventing.EventEnvelopeFactory;
import com.restaurant.platform.eventing.contract.OrderCreatedEvent;
import com.restaurant.platform.eventing.contract.OrderSubmittedToKitchenEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    private final Map<String, OrderEntity> ordersById = new LinkedHashMap<>();
    private final Map<String, List<OrderItemEntity>> itemsByOrderId = new LinkedHashMap<>();
    private final Map<String, OrderStatusHistoryEntity> historyByOrderId = new LinkedHashMap<>();

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository,
                orderItemRepository,
                orderStatusHistoryRepository,
                new EventEnvelopeFactory("test-suite"),
                domainEventPublisher
        );

        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> {
            OrderEntity entity = invocation.getArgument(0);
            if (entity.getOrderedAt() == null) {
                entity.setOrderedAt(Instant.now());
            }
            if (entity.getUpdatedAt() == null) {
                entity.setUpdatedAt(entity.getOrderedAt());
            }
            ordersById.put(entity.getOrderId(), entity);
            return entity;
        });
        when(orderRepository.findByTenantIdAndPropertyIdAndOrderId(anyString(), anyString(), anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(ordersById.get(invocation.getArgument(2))));
        when(orderRepository.findByTenantIdAndPropertyIdOrderByOrderedAtDesc(anyString(), anyString()))
                .thenAnswer(invocation -> ordersById.values().stream()
                        .sorted(Comparator.comparing(OrderEntity::getOrderedAt).reversed())
                        .toList());

        when(orderItemRepository.save(any(OrderItemEntity.class))).thenAnswer(invocation -> {
            OrderItemEntity entity = invocation.getArgument(0);
            itemsByOrderId.computeIfAbsent(entity.getOrderId(), ignored -> new ArrayList<>()).add(entity);
            return entity;
        });
        when(orderItemRepository.saveAll(anyCollection())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            List<OrderItemEntity> entities = new ArrayList<>((List<OrderItemEntity>) invocation.getArgument(0));
            if (!entities.isEmpty()) {
                itemsByOrderId.put(entities.get(0).getOrderId(), entities);
            }
            return entities;
        });
        when(orderItemRepository.findByOrderIdOrderByCreatedAtAsc(anyString()))
                .thenAnswer(invocation -> itemsByOrderId.getOrDefault(invocation.getArgument(0), List.of()));
        when(orderItemRepository.findByOrderIdIn(anyCollection()))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    List<String> orderIds = new ArrayList<>((List<String>) invocation.getArgument(0));
                    return orderIds.stream()
                            .flatMap(orderId -> itemsByOrderId.getOrDefault(orderId, List.of()).stream())
                            .toList();
                });

        when(orderStatusHistoryRepository.findById(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(historyByOrderId.get(invocation.getArgument(0))));
        when(orderStatusHistoryRepository.save(any(OrderStatusHistoryEntity.class))).thenAnswer(invocation -> {
            OrderStatusHistoryEntity entity = invocation.getArgument(0);
            historyByOrderId.put(entity.getOrderId(), entity);
            return entity;
        });
    }

    @Test
    void createOrderPersistsItemsPublishesOrderCreatedEventAndStartsStatusTrail() {
        CreateOrderRequest request = new CreateOrderRequest(
                "krusty-krab",
                "table-01",
                "session-01",
                "emp-01",
                null,
                List.of(
                        new OrderItem("item-001", "Margherita Pizza", 2, BigDecimal.valueOf(299)),
                        new OrderItem("item-002", "Pasta Alfredo", 1, BigDecimal.valueOf(249))
                )
        );

        OrderResponse response = orderService.createOrder("bikini-bottom", "krusty-krab", request);

        assertThat(response.status()).isEqualTo(OrderStatus.CREATED);
        assertThat(response.items()).hasSize(2);
        assertThat(response.items())
                .extracting(OrderItem::quantity)
                .containsExactly(2, 1);

        OrderStatusHistoryEntity history = historyByOrderId.get(response.orderId());
        assertThat(history).isNotNull();
        assertThat(history.getStatus()).isEqualTo(OrderStatus.CREATED.name());
        assertThat(history.getStatusTrail())
                .extracting(OrderStatusTrailEntry::status)
                .containsExactly(OrderStatus.CREATED);

        ArgumentCaptor<EventEnvelope<?>> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(domainEventPublisher).publish(envelopeCaptor.capture());
        assertThat(envelopeCaptor.getValue().payload()).isInstanceOf(OrderCreatedEvent.class);
        OrderCreatedEvent payload = (OrderCreatedEvent) envelopeCaptor.getValue().payload();
        assertThat(payload.sessionId()).isEqualTo("session-01");
        assertThat(payload.items()).hasSize(2);
    }

    @Test
    void submitReadyAndServeAdvanceOrderLifecycleAndPreserveHistoryTrail() {
        OrderResponse created = orderService.createOrder(
                "bikini-bottom",
                "krusty-krab",
                new CreateOrderRequest(
                        "krusty-krab",
                        "table-01",
                        "session-01",
                        "emp-01",
                        null,
                        List.of(new OrderItem("item-001", "Margherita Pizza", 1, BigDecimal.valueOf(299)))
                )
        );

        OrderResponse submitted = orderService.submitToKitchen("bikini-bottom", "krusty-krab", created.orderId());
        OrderResponse ready = orderService.markReadyToServe("bikini-bottom", "krusty-krab", created.orderId());
        OrderResponse served = orderService.markServed("bikini-bottom", "krusty-krab", created.orderId());

        assertThat(submitted.status()).isEqualTo(OrderStatus.IN_KITCHEN);
        assertThat(ready.status()).isEqualTo(OrderStatus.READY_TO_SERVE);
        assertThat(served.status()).isEqualTo(OrderStatus.SERVED);
        assertThat(served.servedAt()).isNotNull();
        assertThat(itemsByOrderId.get(created.orderId()))
                .extracting(OrderItemEntity::getItemStatus)
                .containsExactly(OrderStatus.SERVED.name());

        OrderStatusHistoryEntity history = historyByOrderId.get(created.orderId());
        assertThat(history.getStatus()).isEqualTo(OrderStatus.SERVED.name());
        assertThat(history.getStatusTrail())
                .extracting(OrderStatusTrailEntry::status)
                .containsExactly(
                        OrderStatus.CREATED,
                        OrderStatus.IN_KITCHEN,
                        OrderStatus.READY_TO_SERVE,
                        OrderStatus.SERVED
                );

        ArgumentCaptor<EventEnvelope<?>> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(domainEventPublisher, times(2)).publish(envelopeCaptor.capture());
        assertThat(envelopeCaptor.getAllValues().get(1).payload()).isInstanceOf(OrderSubmittedToKitchenEvent.class);
    }

    @Test
    void markCancelledUpdatesOrderItemsAndKeepsCancellationReason() {
        OrderResponse created = orderService.createOrder(
                "bikini-bottom",
                "krusty-krab",
                new CreateOrderRequest(
                        "krusty-krab",
                        "table-09",
                        "session-09",
                        "emp-09",
                        null,
                        List.of(new OrderItem("item-002", "Pasta Alfredo", 3, BigDecimal.valueOf(249)))
                )
        );

        OrderResponse cancelled = orderService.markCancelled(
                "bikini-bottom",
                "krusty-krab",
                created.orderId(),
                new CancelOrderRequest("Customer changed mind")
        );

        assertThat(cancelled.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(cancelled.cancelledAt()).isNotNull();
        assertThat(cancelled.cancellationReason()).isEqualTo("Customer changed mind");
        assertThat(itemsByOrderId.get(created.orderId()))
                .extracting(OrderItemEntity::getItemStatus)
                .containsExactly(OrderStatus.CANCELLED.name());

        OrderStatusHistoryResponse history = orderService.getOrderStatusHistory("bikini-bottom", "krusty-krab", created.orderId());
        assertThat(history.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(history.statusTrail())
                .extracting(OrderStatusTrailEntry::remarks)
                .contains("Order created.", "Customer changed mind");
    }

    @Test
    void getOrderStatusHistoryThrowsNotFoundForUnknownOrder() {
        assertThatThrownBy(() -> orderService.getOrderStatusHistory("bikini-bottom", "krusty-krab", "missing-order"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Order was not found");
    }
}
