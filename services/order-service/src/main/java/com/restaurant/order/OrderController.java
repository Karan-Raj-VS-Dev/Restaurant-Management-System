package com.restaurant.order;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({
        "/api/orders",
        "/chefy/tenant/{tenantId}/property/{propertyId}/api/orders"
})
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderResponse> listOrders(
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId
    ) {
        return orderService.listOrders(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId)
        );
    }

    @PostMapping
    public OrderResponse createOrder(@PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                     @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                     @RequestParam(name = "tenantId", required = false) String tenantId,
                                     @RequestParam(name = "propertyId", required = false) String propertyId,
                                     @Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId != null ? propertyId : request.propertyId()),
                request
        );
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable String orderId,
                                  @PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                  @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                  @RequestParam(name = "tenantId", required = false) String tenantId,
                                  @RequestParam(name = "propertyId", required = false) String propertyId) {
        return orderService.getOrder(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                orderId
        );
    }

    @PatchMapping("/{orderId}/submit-to-kitchen")
    public OrderResponse submitToKitchen(@PathVariable String orderId,
                                         @PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                         @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                         @RequestParam(name = "tenantId", required = false) String tenantId,
                                         @RequestParam(name = "propertyId", required = false) String propertyId) {
        return orderService.submitToKitchen(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                orderId
        );
    }

    private String resolveTenantId(String pathTenantId, String requestTenantId) {
        if (pathTenantId != null && !pathTenantId.isBlank()) {
            return pathTenantId;
        }
        if (requestTenantId != null && !requestTenantId.isBlank()) {
            return requestTenantId;
        }
        return "bikini-bottom";
    }

    private String resolvePropertyId(String pathPropertyId, String requestPropertyId) {
        if (pathPropertyId != null && !pathPropertyId.isBlank()) {
            return pathPropertyId;
        }
        if (requestPropertyId != null && !requestPropertyId.isBlank()) {
            return requestPropertyId;
        }
        return "krusty-krab";
    }
}
