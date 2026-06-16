package com.restaurant.table;

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
        "/api/tables",
        "/chefy/tenant/{tenantId}/property/{propertyId}/api/tables"
})
public class TableController {

    private final TableService tableService;

    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping
    public List<TableResponse> listTables(
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId
    ) {
        return tableService.listTables(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId)
        );
    }

    @GetMapping("/settings/tables")
    public TableSettingsSummaryResponse getSettingsSummary(
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId
    ) {
        return tableService.getSettingsSummary(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId)
        );
    }

    @PostMapping("/settings/tables")
    public TableSettingRecordResponse createTableSetting(
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId,
            @RequestBody UpsertTableSettingRequest request
    ) {
        return tableService.createTableSetting(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                request
        );
    }

    @PatchMapping("/settings/tables/{tableId}")
    public TableSettingRecordResponse updateTableSetting(
            @PathVariable String tableId,
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId,
            @RequestBody UpsertTableSettingRequest request
    ) {
        return tableService.updateTableSetting(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                tableId,
                request
        );
    }

    @PostMapping("/assign")
    public TableResponse assignTable(@PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                     @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                     @RequestParam(name = "tenantId", required = false) String tenantId,
                                     @RequestParam(name = "propertyId", required = false) String propertyId,
                                     @Valid @RequestBody AssignTableRequest request) {
        return tableService.assignTable(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId != null ? propertyId : request.propertyId()),
                request
        );
    }

    @PatchMapping("/{tableId}/status")
    public TableResponse updateStatus(@PathVariable String tableId,
                                      @PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                      @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                      @RequestParam(name = "tenantId", required = false) String tenantId,
                                      @RequestParam(name = "propertyId", required = false) String propertyId,
                                      @RequestBody UpdateTableStatusRequest request) {
        return tableService.updateStatus(
                resolveTenantId(scopedTenantId, tenantId),
                tableId,
                resolvePropertyId(scopedPropertyId, propertyId),
                request
        );
    }

    @PatchMapping("/{tableId}/session/customer")
    public TableResponse attachCustomerToSession(@PathVariable String tableId,
                                                 @PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                                 @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                                 @RequestParam(name = "tenantId", required = false) String tenantId,
                                                 @RequestParam(name = "propertyId", required = false) String propertyId,
                                                 @Valid @RequestBody AttachTableCustomerRequest request) {
        return tableService.attachCustomerToOpenSession(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                tableId,
                request.customerId()
        );
    }

    @PatchMapping("/{tableId}/session/order")
    public TableResponse attachOrderToSession(@PathVariable String tableId,
                                              @PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                              @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                              @RequestParam(name = "tenantId", required = false) String tenantId,
                                              @RequestParam(name = "propertyId", required = false) String propertyId,
                                              @Valid @RequestBody AttachTableOrderRequest request) {
        return tableService.attachOrderToOpenSession(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                tableId,
                request.orderId()
        );
    }

    @PatchMapping("/{tableId}/needs-cleaning")
    public TableResponse markNeedsCleaning(@PathVariable String tableId,
                                           @PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                           @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                           @RequestParam(name = "tenantId", required = false) String tenantId,
                                           @RequestParam(name = "propertyId", required = false) String propertyId) {
        return tableService.updateStatus(
                resolveTenantId(scopedTenantId, tenantId),
                tableId,
                resolvePropertyId(scopedPropertyId, propertyId),
                new UpdateTableStatusRequest(
                        TableStatus.NEEDS_CLEANING.name(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        true,
                        false
                )
        );
    }

    @PatchMapping("/{tableId}/available")
    public TableResponse markAvailable(@PathVariable String tableId,
                                       @PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                       @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                       @RequestParam(name = "tenantId", required = false) String tenantId,
                                       @RequestParam(name = "propertyId", required = false) String propertyId) {
        return tableService.updateStatus(
                resolveTenantId(scopedTenantId, tenantId),
                tableId,
                resolvePropertyId(scopedPropertyId, propertyId),
                new UpdateTableStatusRequest(
                        TableStatus.AVAILABLE.name(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        true,
                        false
                )
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
