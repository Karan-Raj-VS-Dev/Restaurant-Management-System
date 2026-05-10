package com.restaurant.integration;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({
        "/api/integrations/marketplace",
        "/chefy/tenant/{tenantId}/property/{propertyId}/api/integrations/marketplace"
})
public class MarketplaceIntegrationController {

    private final MarketplaceIntegrationService marketplaceIntegrationService;

    public MarketplaceIntegrationController(MarketplaceIntegrationService marketplaceIntegrationService) {
        this.marketplaceIntegrationService = marketplaceIntegrationService;
    }

    @PostMapping("/orders")
    public MarketplaceOrderResponse ingestOrder(@PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                                @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                                @RequestParam(name = "tenantId", required = false) String tenantId,
                                                @RequestParam(name = "propertyId", required = false) String propertyId,
                                                @Valid @RequestBody MarketplaceOrderRequest request) {
        return marketplaceIntegrationService.ingestOrder(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId != null ? propertyId : request.propertyId()),
                request
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
