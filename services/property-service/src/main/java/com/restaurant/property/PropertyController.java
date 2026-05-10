package com.restaurant.property;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping({
        "/api/properties",
        "/chefy/tenant/{tenantId}/api/properties"
})
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping
    public List<PropertyResponse> listProperties(
            @PathVariable(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "tenantId", required = false) String tenantIdParam
    ) {
        return propertyService.listProperties(resolveTenantId(tenantId, tenantIdParam));
    }

    @GetMapping("/{propertyId}")
    public PropertyResponse getProperty(
            @PathVariable String propertyId,
            @PathVariable(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "tenantId", required = false) String tenantIdParam
    ) {
        return propertyService.getProperty(resolveTenantId(tenantId, tenantIdParam), propertyId);
    }

    @PostMapping
    public PropertyResponse createProperty(
            @PathVariable(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "tenantId", required = false) String tenantIdParam,
            @Valid @RequestBody CreatePropertyRequest request
    ) {
        return propertyService.createProperty(resolveTenantId(tenantId, tenantIdParam), request);
    }

    @PutMapping("/{propertyId}")
    public PropertyResponse updateProperty(
            @PathVariable String propertyId,
            @PathVariable(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "tenantId", required = false) String tenantIdParam,
            @Valid @RequestBody UpdatePropertyRequest request
    ) {
        return propertyService.updateProperty(resolveTenantId(tenantId, tenantIdParam), propertyId, request);
    }

    @DeleteMapping("/{propertyId}")
    public void deleteProperty(
            @PathVariable String propertyId,
            @PathVariable(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "tenantId", required = false) String tenantIdParam
    ) {
        propertyService.deleteProperty(resolveTenantId(tenantId, tenantIdParam), propertyId);
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
}
