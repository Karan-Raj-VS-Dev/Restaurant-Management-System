package com.restaurant.insights;

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
class OperationsInsightsControllerTest {

    @Mock
    private OperationsInsightsService operationsInsightsService;

    private OperationsInsightsController controller;

    @BeforeEach
    void setUp() {
        controller = new OperationsInsightsController(operationsInsightsService);
    }

    @Test
    void getDailyInsightsPrefersPathScope() {
        DailyOperationsInsight expected = new DailyOperationsInsight("krusty-krab", 12, "table-01", "emp-01", 6, BigDecimal.valueOf(3400));
        when(operationsInsightsService.getDailyInsights("tenant-path", "property-path")).thenReturn(expected);

        DailyOperationsInsight response = controller.getDailyInsights("tenant-path", "property-path", "tenant-query", "property-query");

        assertThat(response).isEqualTo(expected);
        verify(operationsInsightsService).getDailyInsights("tenant-path", "property-path");
    }

    @Test
    void getStockInsightsFallsBackToDefaults() {
        List<StockInsight> expected = List.of(new StockInsight("krusty-krab", "Cream", "LOW", 4, "ml"));
        when(operationsInsightsService.getStockInsights("bikini-bottom", "krusty-krab")).thenReturn(expected);

        List<StockInsight> response = controller.getStockInsights(null, null, null, null);

        assertThat(response).isEqualTo(expected);
        verify(operationsInsightsService).getStockInsights("bikini-bottom", "krusty-krab");
    }
}
