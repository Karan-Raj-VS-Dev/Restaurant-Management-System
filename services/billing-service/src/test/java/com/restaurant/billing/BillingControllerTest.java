package com.restaurant.billing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BillingControllerTest {

    @Mock
    private BillingService billingService;

    private BillingController controller;

    @BeforeEach
    void setUp() {
        controller = new BillingController(billingService);
    }

    @Test
    void listBillsUsesResolvedScope() {
        List<BillResponse> expected = List.of(bill("bill-001", BillStatus.DRAFT));
        when(billingService.listBills("tenant-path", "property-path")).thenReturn(expected);

        List<BillResponse> response = controller.listBills("tenant-path", "property-path", "tenant-query", "property-query");

        assertThat(response).isEqualTo(expected);
        verify(billingService).listBills("tenant-path", "property-path");
    }

    @Test
    void createDraftBillDelegatesToService() {
        DraftBillRequest request = new DraftBillRequest(
                "order-001",
                "session-001",
                List.of(new BillLine("item-001", "Margherita Pizza", 2, BigDecimal.valueOf(299)))
        );
        BillResponse expected = bill("bill-001", BillStatus.DRAFT);
        when(billingService.createDraftBill("bikini-bottom", "krusty-krab", request)).thenReturn(expected);

        BillResponse response = controller.createDraftBill(null, null, null, null, request);

        assertThat(response).isEqualTo(expected);
        verify(billingService).createDraftBill("bikini-bottom", "krusty-krab", request);
    }

    @Test
    void finalizeAndGetBillDelegateToService() {
        BillResponse finalized = bill("bill-002", BillStatus.FINALIZED);
        when(billingService.finalizeBill("bikini-bottom", "krusty-krab", "bill-002")).thenReturn(finalized);
        when(billingService.getBill("bikini-bottom", "krusty-krab", "bill-002")).thenReturn(finalized);

        assertThat(controller.finalizeBill("bill-002", null, null, null, null)).isEqualTo(finalized);
        assertThat(controller.getBill("bill-002", null, null, null, null)).isEqualTo(finalized);

        verify(billingService).finalizeBill("bikini-bottom", "krusty-krab", "bill-002");
        verify(billingService).getBill("bikini-bottom", "krusty-krab", "bill-002");
    }

    @Test
    void attachCustomerDelegatesToService() {
        AttachBillCustomerRequest request = new AttachBillCustomerRequest("cust-001");
        BillResponse expected = bill("bill-003", BillStatus.FINALIZED);
        when(billingService.attachCustomer("bikini-bottom", "krusty-krab", "bill-003", "cust-001")).thenReturn(expected);

        BillResponse response = controller.attachCustomer("bill-003", null, null, null, null, request);

        assertThat(response).isEqualTo(expected);
        verify(billingService).attachCustomer("bikini-bottom", "krusty-krab", "bill-003", "cust-001");
    }

    @Test
    void finalizeCancellationDelegatesToService() {
        FinalizeCancellationBillRequest request = new FinalizeCancellationBillRequest("Customer left early", BigDecimal.valueOf(125));
        BillResponse expected = bill("bill-004", BillStatus.FINALIZED);
        when(billingService.finalizeBillCancellation("bikini-bottom", "krusty-krab", "bill-004", request)).thenReturn(expected);

        BillResponse response = controller.finalizeBillCancellation("bill-004", null, null, null, null, request);

        assertThat(response).isEqualTo(expected);
        verify(billingService).finalizeBillCancellation("bikini-bottom", "krusty-krab", "bill-004", request);
    }

    private BillResponse bill(String billId, BillStatus status) {
        return new BillResponse(
                billId,
                "order-001",
                List.of("order-001", "order-002"),
                "table-01",
                "session-01",
                "cust-01",
                status,
                BillSettlementType.STANDARD,
                null,
                BigDecimal.ZERO,
                List.of(new BillLine("item-001", "Margherita Pizza", 2, BigDecimal.valueOf(299))),
                BigDecimal.valueOf(598),
                BigDecimal.valueOf(29.90),
                BigDecimal.valueOf(627.90)
        );
    }
}
