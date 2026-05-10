package com.restaurant.takeaway;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({
        "/api/takeaway/orders",
        "/chefy/tenant/{tenantId}/property/{propertyId}/api/takeaway/orders"
})
public class TakeawayController {

    private final TakeawayService takeawayService;

    public TakeawayController(TakeawayService takeawayService) {
        this.takeawayService = takeawayService;
    }

    @PostMapping
    public TakeawayOrderResponse createTakeawayOrder(@PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                                     @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                                     @RequestParam(name = "tenantId", required = false) String tenantId,
                                                     @RequestParam(name = "propertyId", required = false) String propertyId,
                                                     @Valid @RequestBody CreateTakeawayOrderRequest request) {
        return takeawayService.createTakeawayOrder(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId != null ? propertyId : request.propertyId()),
                request
        );
    }

    @GetMapping("/{takeawayOrderId}")
    public TakeawayOrderResponse getTakeawayOrder(@PathVariable String takeawayOrderId,
                                                  @PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                                  @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                                  @RequestParam(name = "tenantId", required = false) String tenantId,
                                                  @RequestParam(name = "propertyId", required = false) String propertyId) {
        return takeawayService.getTakeawayOrder(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                takeawayOrderId
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
