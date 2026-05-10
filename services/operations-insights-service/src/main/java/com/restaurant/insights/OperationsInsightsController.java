package com.restaurant.insights;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({
        "/api/insights",
        "/chefy/tenant/{tenantId}/property/{propertyId}/api/insights"
})
public class OperationsInsightsController {

    private final OperationsInsightsService operationsInsightsService;

    public OperationsInsightsController(OperationsInsightsService operationsInsightsService) {
        this.operationsInsightsService = operationsInsightsService;
    }

    @GetMapping("/daily")
    public DailyOperationsInsight getDailyInsights(
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId
    ) {
        return operationsInsightsService.getDailyInsights(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId)
        );
    }

    @GetMapping("/stock")
    public List<StockInsight> getStockInsights(
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId
    ) {
        return operationsInsightsService.getStockInsights(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId)
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
