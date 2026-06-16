package com.restaurant.billing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BillingSettingsControllerTest {

    @Mock
    private BillingSettingsService billingSettingsService;

    private BillingSettingsController controller;

    @BeforeEach
    void setUp() {
        controller = new BillingSettingsController(billingSettingsService);
    }

    @Test
    void summaryUsesDefaultScope() {
        BillingSettingsSummaryResponse expected = new BillingSettingsSummaryResponse(List.of(), List.of());
        when(billingSettingsService.getSummary("bikini-bottom", "krusty-krab")).thenReturn(expected);

        assertThat(controller.getSummary(null, null, null, null)).isEqualTo(expected);
        verify(billingSettingsService).getSummary("bikini-bottom", "krusty-krab");
    }

    @Test
    void taxEndpointsDelegateToService() {
        TaxSettingRequest request = new TaxSettingRequest("tax-001", "GST", BigDecimal.valueOf(5), "BILL", "ACTIVE");
        TaxSettingResponse response = new TaxSettingResponse("tax-001", "GST", BigDecimal.valueOf(5), "ACTIVE", "BILL");
        when(billingSettingsService.createTax("tenant-path", "property-path", request)).thenReturn(response);
        when(billingSettingsService.updateTax("tenant-path", "property-path", "tax-001", request)).thenReturn(response);

        assertThat(controller.createTax("tenant-path", "property-path", null, null, request)).isEqualTo(response);
        assertThat(controller.updateTax("tax-001", "tenant-path", "property-path", null, null, request)).isEqualTo(response);
    }

    @Test
    void templateEndpointsDelegateToService() {
        BillingTemplateRequest request = new BillingTemplateRequest("tpl-001", "Standard bill", Map.of("summary", "Default"), "ACTIVE");
        BillingTemplatePlaceholderResponse response = new BillingTemplatePlaceholderResponse("tpl-001", "Standard bill", Map.of("summary", "Default"), "ACTIVE");
        when(billingSettingsService.createTemplate("tenant-path", "property-path", request)).thenReturn(response);
        when(billingSettingsService.updateTemplate("tenant-path", "property-path", "tpl-001", request)).thenReturn(response);

        assertThat(controller.createTemplate("tenant-path", "property-path", null, null, request)).isEqualTo(response);
        assertThat(controller.updateTemplate("tpl-001", "tenant-path", "property-path", null, null, request)).isEqualTo(response);
    }
}
