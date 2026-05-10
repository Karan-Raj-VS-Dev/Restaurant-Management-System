package com.restaurant.property;

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
public class PropertyAreaSectionSettingsController {

    private final PropertyAreaSectionSettingsService propertyAreaSectionSettingsService;

    public PropertyAreaSectionSettingsController(PropertyAreaSectionSettingsService propertyAreaSectionSettingsService) {
        this.propertyAreaSectionSettingsService = propertyAreaSectionSettingsService;
    }

    @GetMapping("/areas-sections")
    public AreaSectionSettingsSummaryResponse getSummary(
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId
    ) {
        return propertyAreaSectionSettingsService.getSummary(resolveTenantId(scopedTenantId, tenantId), resolvePropertyId(scopedPropertyId, propertyId));
    }

    @PostMapping("/areas-sections")
    public AreaSectionSettingResponse create(
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId,
            @RequestBody UpsertAreaSectionSettingRequest request
    ) {
        return propertyAreaSectionSettingsService.create(resolveTenantId(scopedTenantId, tenantId), resolvePropertyId(scopedPropertyId, propertyId), request);
    }

    @PatchMapping("/areas-sections/{areaSectionId}")
    public AreaSectionSettingResponse update(
            @PathVariable String areaSectionId,
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId,
            @RequestBody UpsertAreaSectionSettingRequest request
    ) {
        return propertyAreaSectionSettingsService.update(resolveTenantId(scopedTenantId, tenantId), resolvePropertyId(scopedPropertyId, propertyId), areaSectionId, request);
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
