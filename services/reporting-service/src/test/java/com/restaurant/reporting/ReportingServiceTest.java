package com.restaurant.reporting;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ReportingServiceTest {

    private final ReportingService reportingService = new ReportingService();

    @Test
    void getDailySummaryReturnsExpectedSnapshot() {
        DailySummaryResponse response = reportingService.getDailySummary("bikini-bottom", "krusty-krab");

        assertThat(response.propertyId()).isEqualTo("krusty-krab");
        assertThat(response.totalOrders()).isEqualTo(142);
        assertThat(response.grossSales()).isEqualByComparingTo(BigDecimal.valueOf(45230));
        assertThat(response.completedTakeawayOrders()).isEqualTo(18);
        assertThat(response.pendingKitchenTickets()).isEqualTo(9);
    }
}
