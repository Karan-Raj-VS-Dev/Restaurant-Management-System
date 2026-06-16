package com.restaurant.reporting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportingControllerTest {

    @Mock
    private ReportingService reportingService;

    private ReportingController controller;

    @BeforeEach
    void setUp() {
        controller = new ReportingController(reportingService);
    }

    @Test
    void getDailySummaryPrefersPathScope() {
        DailySummaryResponse expected = new DailySummaryResponse("property-path", 10, BigDecimal.TEN, 2, 1);
        when(reportingService.getDailySummary("tenant-path", "property-path")).thenReturn(expected);

        DailySummaryResponse response = controller.getDailySummary(
                "tenant-path",
                "property-path",
                "tenant-query",
                "property-query"
        );

        assertThat(response).isEqualTo(expected);
        verify(reportingService).getDailySummary("tenant-path", "property-path");
    }

    @Test
    void getDailySummaryFallsBackToDefaults() {
        DailySummaryResponse expected = new DailySummaryResponse("krusty-krab", 142, BigDecimal.valueOf(45230), 18, 9);
        when(reportingService.getDailySummary("bikini-bottom", "krusty-krab")).thenReturn(expected);

        DailySummaryResponse response = controller.getDailySummary(null, null, null, null);

        assertThat(response).isEqualTo(expected);
        verify(reportingService).getDailySummary("bikini-bottom", "krusty-krab");
    }
}
