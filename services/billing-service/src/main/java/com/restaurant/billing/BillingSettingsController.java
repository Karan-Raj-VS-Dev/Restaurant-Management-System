package com.restaurant.billing;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({
        "/api/settings",
        "/chefy/tenant/{tenantId}/property/{propertyId}/api/settings"
})
public class BillingSettingsController {

    private final BillingSettingsService billingSettingsService;

    public BillingSettingsController(BillingSettingsService billingSettingsService) {
        this.billingSettingsService = billingSettingsService;
    }

    @GetMapping("/billing")
    public BillingSettingsSummaryResponse getSummary(
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId
    ) {
        return billingSettingsService.getSummary(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId)
        );
    }

    @PostMapping("/billing/taxes")
    public TaxSettingResponse createTax(
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId,
            @RequestBody TaxSettingRequest request
    ) {
        return billingSettingsService.createTax(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                request
        );
    }

    @PatchMapping("/billing/taxes/{taxId}")
    public TaxSettingResponse updateTax(
            @PathVariable String taxId,
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId,
            @RequestBody TaxSettingRequest request
    ) {
        return billingSettingsService.updateTax(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                taxId,
                request
        );
    }

    @PostMapping("/billing/templates")
    public BillingTemplatePlaceholderResponse createTemplate(
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId,
            @RequestBody BillingTemplateRequest request
    ) {
        return billingSettingsService.createTemplate(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                request
        );
    }

    @PatchMapping("/billing/templates/{templateId}")
    public BillingTemplatePlaceholderResponse updateTemplate(
            @PathVariable String templateId,
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId,
            @RequestBody BillingTemplateRequest request
    ) {
        return billingSettingsService.updateTemplate(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                templateId,
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
