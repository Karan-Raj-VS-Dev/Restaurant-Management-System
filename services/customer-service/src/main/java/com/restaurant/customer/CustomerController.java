package com.restaurant.customer;

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
        "/api/customers",
        "/chefy/tenant/{tenantId}/property/{propertyId}/api/customers"
})
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<CustomerResponse> listCustomers(
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId
    ) {
        return customerService.listCustomers(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId)
        );
    }

    @GetMapping("/{customerId}")
    public CustomerResponse getCustomer(@PathVariable String customerId,
                                        @PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                        @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                        @RequestParam(name = "tenantId", required = false) String tenantId,
                                        @RequestParam(name = "propertyId", required = false) String propertyId) {
        return customerService.getCustomer(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                customerId
        );
    }

    @GetMapping("/lookup")
    public CustomerLookupResponse lookupCustomerByPhone(
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId,
            @RequestParam("phoneNumber") String phoneNumber
    ) {
        return customerService.lookupCustomerByPhone(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                phoneNumber
        );
    }

    @PostMapping
    public CustomerResponse createCustomer(@PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                           @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                           @RequestParam(name = "tenantId", required = false) String tenantId,
                                           @RequestParam(name = "propertyId", required = false) String propertyId,
                                           @Valid @RequestBody CreateCustomerRequest request) {
        return customerService.createCustomer(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
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
