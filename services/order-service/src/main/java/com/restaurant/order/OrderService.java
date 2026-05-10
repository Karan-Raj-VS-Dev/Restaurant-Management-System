package com.restaurant.order;

import com.restaurant.platform.eventing.AggregateTypes;
import com.restaurant.platform.eventing.DomainEventPublisher;
import com.restaurant.platform.eventing.EventEnvelopeFactory;
import com.restaurant.platform.eventing.EventKeys;
import com.restaurant.platform.eventing.contract.OrderCreatedEvent;
import com.restaurant.platform.eventing.contract.OrderLineItem;
import com.restaurant.platform.eventing.contract.OrderSubmittedToKitchenEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderService {

    private final Map<String, ScopedOrder> orders = new ConcurrentHashMap<>();
    private final EventEnvelopeFactory eventEnvelopeFactory;
    private final DomainEventPublisher domainEventPublisher;

    public OrderService(EventEnvelopeFactory eventEnvelopeFactory,
                        DomainEventPublisher domainEventPublisher) {
        this.eventEnvelopeFactory = eventEnvelopeFactory;
        this.domainEventPublisher = domainEventPublisher;
    }

    public List<OrderResponse> listOrders(String tenantId, String propertyId) {
        return orders.values().stream()
                .filter(order -> tenantId.equals(order.tenantId()) && propertyId.equals(order.response().propertyId()))
                .map(ScopedOrder::response)
                .sorted((left, right) -> right.createdAt().compareTo(left.createdAt()))
                .toList();
    }

    public OrderResponse createOrder(String tenantId, String propertyId, CreateOrderRequest request) {
        String orderId = "order-" + UUID.randomUUID();
        OrderResponse response = new OrderResponse(
                orderId,
                propertyId,
                request.tableId(),
                request.waiterId(),
                OrderStatus.CREATED,
                request.items(),
                Instant.now()
        );
        orders.put(orderId, new ScopedOrder(tenantId, response));

        OrderCreatedEvent payload = new OrderCreatedEvent(
                response.orderId(),
                tenantId,
                response.propertyId(),
                response.tableId(),
                response.waiterId(),
                request.customerId(),
                request.items().stream()
                        .map(item -> new OrderLineItem(item.itemId(), item.itemName(), item.quantity()))
                        .toList(),
                response.createdAt()
        );
        domainEventPublisher.publish(eventEnvelopeFactory.create(
                EventKeys.ORDER_CREATED,
                AggregateTypes.ORDER,
                response.orderId(),
                response.propertyId(),
                response.orderId(),
                null,
                payload
        ));
        return response;
    }

    public OrderResponse getOrder(String tenantId, String propertyId, String orderId) {
        ScopedOrder scopedOrder = orders.get(orderId);
        if (scopedOrder != null && tenantId.equals(scopedOrder.tenantId()) && propertyId.equals(scopedOrder.response().propertyId())) {
            return scopedOrder.response();
        }
        return new OrderResponse(
                orderId,
                propertyId,
                "table-02",
                "emp-101",
                OrderStatus.CREATED,
                List.of(new OrderItem("item-001", "Margherita Pizza", 2)),
                Instant.now()
        );
    }

    public OrderResponse submitToKitchen(String tenantId, String propertyId, String orderId) {
        ScopedOrder current = orders.compute(orderId, (key, existing) -> {
            if (existing == null || !tenantId.equals(existing.tenantId()) || !propertyId.equals(existing.response().propertyId())) {
                return new ScopedOrder(tenantId, new OrderResponse(
                        orderId,
                        propertyId,
                        "table-02",
                        "emp-101",
                        OrderStatus.IN_KITCHEN,
                        List.of(new OrderItem("item-001", "Margherita Pizza", 2)),
                        Instant.now()
                ));
            }
            return new ScopedOrder(existing.tenantId(), new OrderResponse(
                    existing.response().orderId(),
                    existing.response().propertyId(),
                    existing.response().tableId(),
                    existing.response().waiterId(),
                    OrderStatus.IN_KITCHEN,
                    existing.response().items(),
                    existing.response().createdAt()
            ));
        });

        OrderSubmittedToKitchenEvent payload = new OrderSubmittedToKitchenEvent(
                current.response().orderId(),
                current.tenantId(),
                current.response().propertyId(),
                current.response().tableId(),
                current.response().waiterId(),
                current.response().items().stream()
                        .map(item -> new OrderLineItem(item.itemId(), item.itemName(), item.quantity()))
                        .toList(),
                Instant.now()
        );
        domainEventPublisher.publish(eventEnvelopeFactory.create(
                EventKeys.ORDER_SUBMITTED_TO_KITCHEN,
                AggregateTypes.ORDER,
                current.response().orderId(),
                current.response().propertyId(),
                current.response().orderId(),
                null,
                payload
        ));
        return current.response();
    }

    private record ScopedOrder(
            String tenantId,
            OrderResponse response
    ) {
    }
}
