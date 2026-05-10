package com.restaurant.billing;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({
        "/api/bills",
        "/chefy/tenant/{tenantId}/property/{propertyId}/api/bills"
})
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping
    public List<BillResponse> listBills(@PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                        @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                        @RequestParam(name = "tenantId", required = false) String tenantId,
                                        @RequestParam(name = "propertyId", required = false) String propertyId) {
        return billingService.listBills(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId)
        );
    }

    @PostMapping("/draft")
    public BillResponse createDraftBill(@PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                        @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                        @RequestParam(name = "tenantId", required = false) String tenantId,
                                        @RequestParam(name = "propertyId", required = false) String propertyId,
                                        @Valid @RequestBody DraftBillRequest request) {
        return billingService.createDraftBill(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                request
        );
    }

    @PostMapping("/{billId}/finalize")
    public BillResponse finalizeBill(@PathVariable String billId,
                                     @PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                     @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                     @RequestParam(name = "tenantId", required = false) String tenantId,
                                     @RequestParam(name = "propertyId", required = false) String propertyId) {
        return billingService.finalizeBill(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                billId
        );
    }

    @GetMapping("/{billId}")
    public BillResponse getBill(@PathVariable String billId,
                                @PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                @RequestParam(name = "tenantId", required = false) String tenantId,
                                @RequestParam(name = "propertyId", required = false) String propertyId) {
        return billingService.getBill(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                billId
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
