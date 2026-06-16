package com.restaurant.insights;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.restaurant.platform.eventing.contract.OrderCreatedEvent;
import com.restaurant.platform.eventing.contract.OrderLineItem;
import com.restaurant.platform.eventing.contract.PaymentSucceededEvent;
import com.restaurant.platform.eventing.contract.StockAlertEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OperationsInsightsServiceTest {

    @Mock
    private OperationsInsightsProjection projection;

    private OperationsInsightsService service;

    @BeforeEach
    void setUp() {
        service = new OperationsInsightsService(projection);
    }

    @Test
    void getDailyInsightsMapsSnapshot() {
        when(projection.snapshot("bikini-bottom", "krusty-krab"))
                .thenReturn(new DailySnapshot("bikini-bottom", "krusty-krab", 11, "table-02", "emp-10", 5, BigDecimal.valueOf(4200)));

        DailyOperationsInsight response = service.getDailyInsights("bikini-bottom", "krusty-krab");

        assertThat(response.propertyId()).isEqualTo("krusty-krab");
        assertThat(response.totalOrdersToday()).isEqualTo(11);
        assertThat(response.busiestTableId()).isEqualTo("table-02");
        assertThat(response.topServerId()).isEqualTo("emp-10");
        assertThat(response.topServerCustomerCount()).isEqualTo(5);
        assertThat(response.grossSalesToday()).isEqualByComparingTo(BigDecimal.valueOf(4200));
    }

    @Test
    void getStockInsightsMapsProjectionRows() {
        when(projection.stockSnapshot("bikini-bottom", "krusty-krab"))
                .thenReturn(List.of(new StockHealthView("bikini-bottom", "krusty-krab", "Cream", "LOW", 2, "ml")));

        List<StockInsight> response = service.getStockInsights("bikini-bottom", "krusty-krab");

        assertThat(response).containsExactly(new StockInsight("krusty-krab", "Cream", "LOW", 2, "ml"));
    }

    @Test
    void eventHandlersDelegateToProjection() {
        service.recordOrder(new OrderCreatedEvent(
                "order-001",
                "bikini-bottom",
                "krusty-krab",
                "table-01",
                "session-01",
                "emp-01",
                "cust-01",
                List.of(new OrderLineItem("menu-01", "Margherita Pizza", 1)),
                Instant.parse("2026-06-15T10:00:00Z")
        ));
        service.recordSale(new PaymentSucceededEvent(
                "pay-001", "bill-001", "bikini-bottom", "krusty-krab", BigDecimal.valueOf(550), "CARD", Instant.parse("2026-06-15T10:10:00Z")
        ));
        service.updateStock(new StockAlertEvent(
                "bikini-bottom", "krusty-krab", "ing-001", "Cream", "LOW", 2, "ml", Instant.parse("2026-06-15T10:11:00Z")
        ));

        verify(projection).recordOrder("bikini-bottom", "krusty-krab", "table-01", "emp-01");
        verify(projection).recordSale("bikini-bottom", "krusty-krab", BigDecimal.valueOf(550));
        verify(projection).updateStock(new StockHealthView("bikini-bottom", "krusty-krab", "Cream", "LOW", 2, "ml"));
    }
}
