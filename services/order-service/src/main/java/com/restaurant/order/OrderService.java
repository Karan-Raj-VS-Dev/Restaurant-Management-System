package com.restaurant.order;

import com.restaurant.order.persistence.entity.OrderEntity;
import com.restaurant.order.persistence.entity.OrderItemEntity;
import com.restaurant.order.persistence.repository.OrderItemRepository;
import com.restaurant.order.persistence.repository.OrderRepository;
import com.restaurant.platform.eventing.AggregateTypes;
import com.restaurant.platform.eventing.DomainEventPublisher;
import com.restaurant.platform.eventing.EventEnvelopeFactory;
import com.restaurant.platform.eventing.EventKeys;
import com.restaurant.platform.eventing.contract.OrderCreatedEvent;
import com.restaurant.platform.eventing.contract.OrderLineItem;
import com.restaurant.platform.eventing.contract.OrderSubmittedToKitchenEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class OrderService {

    private static final BigDecimal ZERO_AMOUNT = BigDecimal.ZERO.setScale(2);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final EventEnvelopeFactory eventEnvelopeFactory;
    private final DomainEventPublisher domainEventPublisher;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        EventEnvelopeFactory eventEnvelopeFactory,
                        DomainEventPublisher domainEventPublisher) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.eventEnvelopeFactory = eventEnvelopeFactory;
        this.domainEventPublisher = domainEventPublisher;
    }

    public List<OrderResponse> listOrders(String tenantId, String propertyId) {
        List<OrderEntity> orders = orderRepository.findByTenantIdAndPropertyIdOrderByOrderedAtDesc(tenantId, propertyId);
        Map<String, List<OrderItemEntity>> itemsByOrderId = loadItemsByOrderId(
                orders.stream().map(OrderEntity::getOrderId).toList()
        );
        return orders.stream()
                .map(order -> toResponse(order, itemsByOrderId.getOrDefault(order.getOrderId(), List.of())))
                .toList();
    }

    public OrderResponse createOrder(String tenantId, String propertyId, CreateOrderRequest request) {
        String orderId = "order-" + UUID.randomUUID();
        Instant now = Instant.now();

        OrderEntity entity = new OrderEntity();
        entity.setOrderId(orderId);
        entity.setTenantId(tenantId);
        entity.setPropertyId(propertyId);
        entity.setTableId(request.tableId());
        entity.setSessionId(null);
        entity.setCustomerId(blankToNull(request.customerId()));
        entity.setWaiterId(request.waiterId());
        entity.setOrderType("DINE_IN");
        entity.setOrderStatus(OrderStatus.CREATED.name());
        entity.setGuestCount(1);
        entity.setSubtotalAmount(ZERO_AMOUNT);
        entity.setTaxAmount(ZERO_AMOUNT);
        entity.setDiscountAmount(ZERO_AMOUNT);
        entity.setTotalAmount(ZERO_AMOUNT);
        entity.setSpecialInstructions(null);
        entity.setOrderedAt(now);
        entity.setUpdatedAt(now);
        orderRepository.save(entity);

        List<OrderItemEntity> savedItems = saveOrderItems(orderId, request.items());
        OrderResponse response = toResponse(entity, savedItems);

        OrderCreatedEvent payload = new OrderCreatedEvent(
                response.orderId(),
                tenantId,
                response.propertyId(),
                response.tableId(),
                response.waiterId(),
                request.customerId(),
                response.items().stream()
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
        OrderEntity entity = orderRepository.findByTenantIdAndPropertyIdAndOrderId(tenantId, propertyId, orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order was not found."));
        return toResponse(entity, orderItemRepository.findByOrderIdOrderByCreatedAtAsc(orderId));
    }

    public OrderResponse submitToKitchen(String tenantId, String propertyId, String orderId) {
        OrderEntity order = updateStatus(tenantId, propertyId, orderId, OrderStatus.IN_KITCHEN);
        List<OrderItemEntity> orderItems = orderItemRepository.findByOrderIdOrderByCreatedAtAsc(orderId);

        OrderSubmittedToKitchenEvent payload = new OrderSubmittedToKitchenEvent(
                order.getOrderId(),
                order.getTenantId(),
                order.getPropertyId(),
                order.getTableId(),
                order.getWaiterId(),
                orderItems.stream()
                        .map(item -> new OrderLineItem(item.getMenuItemId(), item.getItemName(), item.getQuantity()))
                        .toList(),
                Instant.now()
        );
        domainEventPublisher.publish(eventEnvelopeFactory.create(
                EventKeys.ORDER_SUBMITTED_TO_KITCHEN,
                AggregateTypes.ORDER,
                order.getOrderId(),
                order.getPropertyId(),
                order.getOrderId(),
                null,
                payload
        ));
        return toResponse(order, orderItems);
    }

    public OrderResponse markReadyToServe(String tenantId, String propertyId, String orderId) {
        OrderEntity order = updateStatus(tenantId, propertyId, orderId, OrderStatus.READY_TO_SERVE);
        return toResponse(order, orderItemRepository.findByOrderIdOrderByCreatedAtAsc(orderId));
    }

    public OrderResponse markServed(String tenantId, String propertyId, String orderId) {
        OrderEntity order = updateStatus(tenantId, propertyId, orderId, OrderStatus.SERVED);
        Instant now = Instant.now();
        order.setServedAt(now);
        order.setCancelledAt(null);
        order.setCancellationReason(null);
        List<OrderItemEntity> items = orderItemRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
        items.forEach(item -> item.setItemStatus(OrderStatus.SERVED.name()));
        orderItemRepository.saveAll(items);
        return toResponse(orderRepository.save(order), items);
    }

    public OrderResponse markCancelled(String tenantId, String propertyId, String orderId, CancelOrderRequest request) {
        OrderEntity order = updateStatus(tenantId, propertyId, orderId, OrderStatus.CANCELLED);
        Instant now = Instant.now();
        order.setCancelledAt(now);
        order.setServedAt(null);
        order.setCancellationReason(request.reason().trim());
        List<OrderItemEntity> items = orderItemRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
        items.forEach(item -> item.setItemStatus(OrderStatus.CANCELLED.name()));
        orderItemRepository.saveAll(items);
        return toResponse(orderRepository.save(order), items);
    }

    private OrderEntity updateStatus(String tenantId, String propertyId, String orderId, OrderStatus status) {
        OrderEntity entity = orderRepository.findByTenantIdAndPropertyIdAndOrderId(tenantId, propertyId, orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order was not found."));
        entity.setOrderStatus(status.name());
        entity.setUpdatedAt(Instant.now());
        return orderRepository.save(entity);
    }

    private List<OrderItemEntity> saveOrderItems(String orderId, List<OrderItem> items) {
        return items.stream().map(item -> {
            OrderItemEntity entity = new OrderItemEntity();
            BigDecimal unitPrice = item.unitPrice() == null ? ZERO_AMOUNT : item.unitPrice().setScale(2);
            entity.setOrderItemId("order-item-" + UUID.randomUUID());
            entity.setOrderId(orderId);
            entity.setMenuItemId(item.itemId());
            entity.setItemName(item.itemName());
            entity.setQuantity(item.quantity());
            entity.setUnitPrice(unitPrice);
            entity.setLineTotal(unitPrice.multiply(BigDecimal.valueOf(item.quantity())).setScale(2));
            entity.setItemStatus(OrderStatus.CREATED.name());
            entity.setNotes(null);
            return orderItemRepository.save(entity);
        }).toList();
    }

    private Map<String, List<OrderItemEntity>> loadItemsByOrderId(Collection<String> orderIds) {
        if (orderIds.isEmpty()) {
            return Map.of();
        }
        return orderItemRepository.findByOrderIdIn(orderIds).stream()
                .collect(Collectors.groupingBy(OrderItemEntity::getOrderId));
    }

    private OrderResponse toResponse(OrderEntity entity, List<OrderItemEntity> items) {
        return new OrderResponse(
                entity.getOrderId(),
                entity.getPropertyId(),
                entity.getTableId(),
                entity.getWaiterId(),
                OrderStatus.valueOf(entity.getOrderStatus()),
                items.stream()
                        .map(item -> new OrderItem(item.getMenuItemId(), item.getItemName(), item.getQuantity(), item.getUnitPrice()))
                        .toList(),
                entity.getOrderedAt(),
                entity.getServedAt(),
                entity.getCancelledAt(),
                entity.getCancellationReason()
        );
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
