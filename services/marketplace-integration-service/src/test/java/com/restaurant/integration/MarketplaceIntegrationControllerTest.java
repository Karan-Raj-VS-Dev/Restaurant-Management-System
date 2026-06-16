package com.restaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarketplaceIntegrationControllerTest {

    @Mock
    private MarketplaceIntegrationService marketplaceIntegrationService;

    @Test
    void ingestOrderUsesScopedIdsBeforeRequestValues() {
        MarketplaceIntegrationController controller = new MarketplaceIntegrationController(marketplaceIntegrationService);
        MarketplaceOrderRequest request = new MarketplaceOrderRequest("Swiggy", "ext-001", "property-request");
        MarketplaceOrderResponse response = new MarketplaceOrderResponse(
                "mp-001",
                "tenant-path",
                "property-path",
                "Swiggy",
                "ext-001",
                null,
                "ACCEPTED_FOR_ASYNC_PROCESSING",
                Instant.parse("2026-06-15T07:00:00Z")
        );
        when(marketplaceIntegrationService.ingestOrder("tenant-path", "property-path", request)).thenReturn(response);

        MarketplaceOrderResponse result = controller.ingestOrder(
                "tenant-path",
                "property-path",
                "tenant-query",
                "property-query",
                request
        );

        assertThat(result).isEqualTo(response);
        verify(marketplaceIntegrationService).ingestOrder("tenant-path", "property-path", request);
    }

    @Test
    void ingestOrderFallsBackToDefaultsWhenScopeMissing() {
        MarketplaceIntegrationController controller = new MarketplaceIntegrationController(marketplaceIntegrationService);
        MarketplaceOrderRequest request = new MarketplaceOrderRequest("Zomato", "ext-002", "property-request");
        MarketplaceOrderResponse response = new MarketplaceOrderResponse(
                "mp-002",
                "bikini-bottom",
                "property-request",
                "Zomato",
                "ext-002",
                null,
                "ACCEPTED_FOR_ASYNC_PROCESSING",
                Instant.parse("2026-06-15T07:05:00Z")
        );
        when(marketplaceIntegrationService.ingestOrder("bikini-bottom", "property-request", request)).thenReturn(response);

        MarketplaceOrderResponse result = controller.ingestOrder(null, null, null, null, request);

        assertThat(result).isEqualTo(response);
        verify(marketplaceIntegrationService).ingestOrder("bikini-bottom", "property-request", request);
    }
}
