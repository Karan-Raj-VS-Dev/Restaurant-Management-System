package com.restaurant.table;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TableControllerTest {

    @Mock
    private TableService tableService;

    private TableController controller;

    @BeforeEach
    void setUp() {
        controller = new TableController(tableService);
    }

    @Test
    void listTablesUsesResolvedScope() {
        List<TableResponse> expected = List.of(table("table-01", TableStatus.AVAILABLE));
        when(tableService.listTables("tenant-path", "property-path")).thenReturn(expected);

        List<TableResponse> response = controller.listTables("tenant-path", "property-path", "tenant-query", "property-query");

        assertThat(response).isEqualTo(expected);
        verify(tableService).listTables("tenant-path", "property-path");
    }

    @Test
    void settingsEndpointsDelegateToService() {
        TableSettingsSummaryResponse summary = new TableSettingsSummaryResponse(1, List.of("displayName"), List.of(
                new TableSettingRecordResponse("table-01", "T-01", "Main Table", "Main floor", "Dining", 4, "AVAILABLE", true)
        ));
        UpsertTableSettingRequest request = new UpsertTableSettingRequest("T-02", "Window 02", "Main floor", "Patio", 2, "AVAILABLE", true);
        TableSettingRecordResponse created = new TableSettingRecordResponse("table-02", "T-02", "Window 02", "Main floor", "Patio", 2, "AVAILABLE", true);
        TableSettingRecordResponse updated = new TableSettingRecordResponse("table-02", "T-02", "Window 02", "Main floor", "Patio", 2, "OCCUPIED", true);
        when(tableService.getSettingsSummary("bikini-bottom", "krusty-krab")).thenReturn(summary);
        when(tableService.createTableSetting("bikini-bottom", "krusty-krab", request)).thenReturn(created);
        when(tableService.updateTableSetting("bikini-bottom", "krusty-krab", "table-02", request)).thenReturn(updated);

        assertThat(controller.getSettingsSummary(null, null, null, null)).isEqualTo(summary);
        assertThat(controller.createTableSetting(null, null, null, null, request)).isEqualTo(created);
        assertThat(controller.updateTableSetting("table-02", null, null, null, null, request)).isEqualTo(updated);

        verify(tableService).getSettingsSummary("bikini-bottom", "krusty-krab");
        verify(tableService).createTableSetting("bikini-bottom", "krusty-krab", request);
        verify(tableService).updateTableSetting("bikini-bottom", "krusty-krab", "table-02", request);
    }

    @Test
    void assignTableUsesPropertyFromRequestWhenMissingFromScope() {
        AssignTableRequest request = new AssignTableRequest("table-03", "krusty-krab", 6, "emp-03");
        TableResponse expected = table("table-03", TableStatus.OCCUPIED);
        when(tableService.assignTable("bikini-bottom", "krusty-krab", request)).thenReturn(expected);

        TableResponse response = controller.assignTable(null, null, null, null, request);

        assertThat(response).isEqualTo(expected);
        verify(tableService).assignTable("bikini-bottom", "krusty-krab", request);
    }

    @Test
    void updateStatusDelegatesWithResolvedScope() {
        UpdateTableStatusRequest request = new UpdateTableStatusRequest(
                "RESERVED",
                4,
                "emp-01",
                "emp-99",
                4,
                Instant.parse("2026-06-15T10:45:00Z"),
                true,
                false
        );
        TableResponse expected = table("table-01", TableStatus.RESERVED);
        when(tableService.updateStatus("bikini-bottom", "table-01", "krusty-krab", request)).thenReturn(expected);

        TableResponse response = controller.updateStatus("table-01", null, null, null, null, request);

        assertThat(response).isEqualTo(expected);
        verify(tableService).updateStatus("bikini-bottom", "table-01", "krusty-krab", request);
    }

    @Test
    void sessionAttachmentEndpointsDelegateToService() {
        TableResponse attachedCustomer = table("table-01", TableStatus.OCCUPIED);
        TableResponse attachedOrder = table("table-01", TableStatus.OCCUPIED);
        when(tableService.attachCustomerToOpenSession("bikini-bottom", "krusty-krab", "table-01", "cust-001")).thenReturn(attachedCustomer);
        when(tableService.attachOrderToOpenSession("bikini-bottom", "krusty-krab", "table-01", "order-001")).thenReturn(attachedOrder);

        assertThat(controller.attachCustomerToSession("table-01", null, null, null, null, new AttachTableCustomerRequest("cust-001")))
                .isEqualTo(attachedCustomer);
        assertThat(controller.attachOrderToSession("table-01", null, null, null, null, new AttachTableOrderRequest("order-001")))
                .isEqualTo(attachedOrder);

        verify(tableService).attachCustomerToOpenSession("bikini-bottom", "krusty-krab", "table-01", "cust-001");
        verify(tableService).attachOrderToOpenSession("bikini-bottom", "krusty-krab", "table-01", "order-001");
    }

    @Test
    void markNeedsCleaningAndAvailableBuildExpectedStatusRequests() {
        TableResponse expected = table("table-01", TableStatus.NEEDS_CLEANING);
        when(tableService.updateStatus(org.mockito.ArgumentMatchers.eq("bikini-bottom"), org.mockito.ArgumentMatchers.eq("table-01"), org.mockito.ArgumentMatchers.eq("krusty-krab"), org.mockito.ArgumentMatchers.any(UpdateTableStatusRequest.class)))
                .thenReturn(expected);

        controller.markNeedsCleaning("table-01", null, null, null, null);
        ArgumentCaptor<UpdateTableStatusRequest> cleaningCaptor = ArgumentCaptor.forClass(UpdateTableStatusRequest.class);
        verify(tableService).updateStatus(org.mockito.ArgumentMatchers.eq("bikini-bottom"), org.mockito.ArgumentMatchers.eq("table-01"), org.mockito.ArgumentMatchers.eq("krusty-krab"), cleaningCaptor.capture());
        assertThat(cleaningCaptor.getValue().targetStatus()).isEqualTo(TableStatus.NEEDS_CLEANING.name());
        assertThat(cleaningCaptor.getValue().immediate()).isTrue();

        controller.markAvailable("table-01", null, null, null, null);
        ArgumentCaptor<UpdateTableStatusRequest> availableCaptor = ArgumentCaptor.forClass(UpdateTableStatusRequest.class);
        verify(tableService, org.mockito.Mockito.times(2)).updateStatus(org.mockito.ArgumentMatchers.eq("bikini-bottom"), org.mockito.ArgumentMatchers.eq("table-01"), org.mockito.ArgumentMatchers.eq("krusty-krab"), availableCaptor.capture());
        UpdateTableStatusRequest availableRequest = availableCaptor.getAllValues().get(1);
        assertThat(availableRequest.targetStatus()).isEqualTo(TableStatus.AVAILABLE.name());
        assertThat(availableRequest.immediate()).isTrue();
    }

    private TableResponse table(String tableId, TableStatus status) {
        return new TableResponse(
                tableId,
                "T-01",
                "Main Table",
                "krusty-krab",
                "Main floor",
                "Dining",
                4,
                status,
                "emp-01",
                "emp-02",
                2,
                null,
                null,
                null,
                null,
                "session-01",
                "order-01",
                "cust-01"
        );
    }
}
