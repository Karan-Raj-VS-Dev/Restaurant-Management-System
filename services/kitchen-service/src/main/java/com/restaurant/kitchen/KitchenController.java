package com.restaurant.kitchen;

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
        "/api/kitchen",
        "/chefy/tenant/{tenantId}/property/{propertyId}/api/kitchen"
})
public class KitchenController {

    private final KitchenService kitchenService;

    public KitchenController(KitchenService kitchenService) {
        this.kitchenService = kitchenService;
    }

    @GetMapping("/tickets")
    public List<KitchenTicketResponse> listTickets(
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId
    ) {
        return kitchenService.listTickets(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId)
        );
    }

    @PostMapping("/tickets")
    public KitchenTicketResponse createTicket(@PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                              @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                              @RequestParam(name = "tenantId", required = false) String tenantId,
                                              @RequestParam(name = "propertyId", required = false) String propertyId,
                                              @Valid @RequestBody CreateKitchenTicketRequest request) {
        return kitchenService.createTicket(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId != null ? propertyId : request.propertyId()),
                request
        );
    }

    @PatchMapping("/tickets/{ticketId}/accept")
    public KitchenTicketResponse acceptTicket(@PathVariable String ticketId,
                                              @PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                              @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                              @RequestParam(name = "tenantId", required = false) String tenantId,
                                              @RequestParam(name = "propertyId", required = false) String propertyId,
                                              @RequestBody(required = false) UpdateKitchenTicketRequest request) {
        return kitchenService.acceptTicket(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                ticketId,
                request
        );
    }

    @PatchMapping("/tickets/{ticketId}/ready")
    public KitchenTicketResponse markReady(@PathVariable String ticketId,
                                           @PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                           @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                           @RequestParam(name = "tenantId", required = false) String tenantId,
                                           @RequestParam(name = "propertyId", required = false) String propertyId,
                                           @RequestBody(required = false) UpdateKitchenTicketRequest request) {
        return kitchenService.markReady(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                ticketId,
                request
        );
    }

    @PatchMapping("/tickets/{ticketId}/served")
    public KitchenTicketResponse markServed(@PathVariable String ticketId,
                                            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                            @RequestParam(name = "tenantId", required = false) String tenantId,
                                            @RequestParam(name = "propertyId", required = false) String propertyId) {
        return kitchenService.markServed(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                ticketId
        );
    }

    @PatchMapping("/tickets/{ticketId}/cancelled")
    public KitchenTicketResponse markCancelled(@PathVariable String ticketId,
                                               @PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                               @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                               @RequestParam(name = "tenantId", required = false) String tenantId,
                                               @RequestParam(name = "propertyId", required = false) String propertyId,
                                               @RequestBody(required = false) UpdateKitchenTicketRequest request) {
        return kitchenService.markCancelled(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                ticketId,
                request
        );
    }

    @PatchMapping("/tickets/{ticketId}/dumped")
    public KitchenTicketResponse markDumped(@PathVariable String ticketId,
                                            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                            @RequestParam(name = "tenantId", required = false) String tenantId,
                                            @RequestParam(name = "propertyId", required = false) String propertyId) {
        return kitchenService.markDumped(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                ticketId
        );
    }

    @PatchMapping("/tickets/{ticketId}/reused")
    public KitchenTicketResponse markReused(@PathVariable String ticketId,
                                            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                            @RequestParam(name = "tenantId", required = false) String tenantId,
                                            @RequestParam(name = "propertyId", required = false) String propertyId,
                                            @RequestBody(required = false) UpdateKitchenTicketRequest request) {
        return kitchenService.markReused(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                ticketId,
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
