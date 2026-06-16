package com.restaurant.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    private OrderController controller;

    @BeforeEach
    void setUp() {
        controller = new OrderController(orderService);
    }

    @Test
    void listOrdersPrefersPathScopedTenantAndProperty() {
        List<OrderResponse> expected = List.of(orderResponse("order-001", OrderStatus.CREATED));
        when(orderService.listOrders("tenant-path", "property-path")).thenReturn(expected);

        List<OrderResponse> response = controller.listOrders("tenant-path", "property-path", "tenant-query", "property-query");

        assertThat(response).isEqualTo(expected);
        verify(orderService).listOrders("tenant-path", "property-path");
    }

    @Test
    void createOrderUsesRequestPropertyWhenQueryPropertyMissing() {
        CreateOrderRequest request = new CreateOrderRequest(
                "krusty-krab",
                "table-01",
                "session-01",
                "emp-01",
                null,
                List.of(new OrderItem("item-001", "Margherita Pizza", 2, BigDecimal.valueOf(299)))
        );
        OrderResponse expected = orderResponse("order-001", OrderStatus.CREATED);
        when(orderService.createOrder("bikini-bottom", "krusty-krab", request)).thenReturn(expected);

        OrderResponse response = controller.createOrder(null, null, null, null, request);

        assertThat(response).isEqualTo(expected);
        verify(orderService).createOrder("bikini-bottom", "krusty-krab", request);
    }

    @Test
    void getOrderUsesDefaultScopeWhenNothingElseProvided() {
        OrderResponse expected = orderResponse("order-002", OrderStatus.IN_KITCHEN);
        when(orderService.getOrder("bikini-bottom", "krusty-krab", "order-002")).thenReturn(expected);

        OrderResponse response = controller.getOrder("order-002", null, null, null, null);

        assertThat(response).isEqualTo(expected);
        verify(orderService).getOrder("bikini-bottom", "krusty-krab", "order-002");
    }

    @Test
    void statusEndpointsDelegateToService() {
        OrderResponse submitted = orderResponse("order-003", OrderStatus.IN_KITCHEN);
        OrderResponse ready = orderResponse("order-003", OrderStatus.READY_TO_SERVE);
        OrderResponse served = orderResponse("order-003", OrderStatus.SERVED);
        CancelOrderRequest cancelRequest = new CancelOrderRequest("Customer changed mind");
        OrderResponse cancelled = orderResponse("order-003", OrderStatus.CANCELLED);
        when(orderService.submitToKitchen("bikini-bottom", "krusty-krab", "order-003")).thenReturn(submitted);
        when(orderService.markReadyToServe("bikini-bottom", "krusty-krab", "order-003")).thenReturn(ready);
        when(orderService.markServed("bikini-bottom", "krusty-krab", "order-003")).thenReturn(served);
        when(orderService.markCancelled("bikini-bottom", "krusty-krab", "order-003", cancelRequest)).thenReturn(cancelled);

        assertThat(controller.submitToKitchen("order-003", null, null, null, null)).isEqualTo(submitted);
        assertThat(controller.markReadyToServe("order-003", null, null, null, null)).isEqualTo(ready);
        assertThat(controller.markServed("order-003", null, null, null, null)).isEqualTo(served);
        assertThat(controller.markCancelled("order-003", null, null, null, null, cancelRequest)).isEqualTo(cancelled);

        verify(orderService).submitToKitchen("bikini-bottom", "krusty-krab", "order-003");
        verify(orderService).markReadyToServe("bikini-bottom", "krusty-krab", "order-003");
        verify(orderService).markServed("bikini-bottom", "krusty-krab", "order-003");
        verify(orderService).markCancelled("bikini-bottom", "krusty-krab", "order-003", cancelRequest);
    }

    @Test
    void statusHistoryDelegatesToService() {
        OrderStatusHistoryResponse expected = new OrderStatusHistoryResponse(
                "order-004",
                OrderStatus.SERVED,
                List.of(
                        new OrderStatusTrailEntry(OrderStatus.CREATED, "emp-01", "Created", Instant.parse("2026-06-15T10:00:00Z")),
                        new OrderStatusTrailEntry(OrderStatus.SERVED, "emp-01", "Served", Instant.parse("2026-06-15T10:20:00Z"))
                ),
                Instant.parse("2026-06-15T10:20:00Z")
        );
        when(orderService.getOrderStatusHistory("bikini-bottom", "krusty-krab", "order-004")).thenReturn(expected);

        OrderStatusHistoryResponse response = controller.getOrderStatusHistory("order-004", null, null, null, null);

        assertThat(response).isEqualTo(expected);
        verify(orderService).getOrderStatusHistory("bikini-bottom", "krusty-krab", "order-004");
    }

    private OrderResponse orderResponse(String orderId, OrderStatus status) {
        return new OrderResponse(
                orderId,
                "krusty-krab",
                "table-01",
                "emp-01",
                status,
                List.of(new OrderItem("item-001", "Margherita Pizza", 1, BigDecimal.valueOf(299))),
                Instant.parse("2026-06-15T10:00:00Z"),
                status == OrderStatus.SERVED ? Instant.parse("2026-06-15T10:15:00Z") : null,
                status == OrderStatus.CANCELLED ? Instant.parse("2026-06-15T10:16:00Z") : null,
                status == OrderStatus.CANCELLED ? "Customer left" : null
        );
    }
}
