package com.restaurant.billing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.restaurant.billing.persistence.entity.BillingTemplateEntity;
import com.restaurant.billing.persistence.entity.TaxDefinitionEntity;
import com.restaurant.billing.persistence.repository.BillingTemplateRepository;
import com.restaurant.billing.persistence.repository.TaxDefinitionRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BillingSettingsServiceTest {

    @Mock
    private TaxDefinitionRepository taxDefinitionRepository;

    @Mock
    private BillingTemplateRepository billingTemplateRepository;

    private BillingSettingsService service;

    @BeforeEach
    void setUp() {
        service = new BillingSettingsService(taxDefinitionRepository, billingTemplateRepository);
    }

    @Test
    void getSummaryMapsRepositoryResults() {
        when(taxDefinitionRepository.findByTenantIdAndPropertyIdOrderByTaxNameAsc("bikini-bottom", "krusty-krab"))
                .thenReturn(List.of(TaxDefinitionEntity.create("tax-001", "bikini-bottom", "krusty-krab", "GST", BigDecimal.valueOf(5), "BILL", "ACTIVE")));
        when(billingTemplateRepository.findByTenantIdAndPropertyIdOrderByTemplateNameAsc("bikini-bottom", "krusty-krab"))
                .thenReturn(List.of(BillingTemplateEntity.create("tpl-001", "bikini-bottom", "krusty-krab", "Standard", Map.of("summary", "Default"), "ACTIVE")));

        BillingSettingsSummaryResponse response = service.getSummary("bikini-bottom", "krusty-krab");

        assertThat(response.taxes()).hasSize(1);
        assertThat(response.templates()).hasSize(1);
        assertThat(response.taxes().get(0).taxName()).isEqualTo("GST");
        assertThat(response.templates().get(0).templateName()).isEqualTo("Standard");
    }

    @Test
    void createTaxPersistsMappedEntity() {
        TaxSettingRequest request = new TaxSettingRequest("tax-001", "GST", BigDecimal.valueOf(5), "BILL", "ACTIVE");

        TaxSettingResponse response = service.createTax("bikini-bottom", "krusty-krab", request);

        assertThat(response.taxId()).isEqualTo("tax-001");
        verify(taxDefinitionRepository).save(any(TaxDefinitionEntity.class));
    }

    @Test
    void updateTaxReusesExistingEntityWhenPresent() {
        TaxDefinitionEntity existing = TaxDefinitionEntity.create("tax-001", "bikini-bottom", "krusty-krab", "Old GST", BigDecimal.ONE, "BILL", "INACTIVE");
        when(taxDefinitionRepository.findById("tax-001")).thenReturn(Optional.of(existing));

        TaxSettingResponse response = service.updateTax(
                "bikini-bottom",
                "krusty-krab",
                "tax-001",
                new TaxSettingRequest("tax-001", "GST", BigDecimal.valueOf(5), "BILL", "ACTIVE")
        );

        assertThat(response.taxName()).isEqualTo("GST");
        assertThat(existing.getStatus()).isEqualTo("ACTIVE");
        verify(taxDefinitionRepository).save(existing);
    }

    @Test
    void createTemplateUsesSafeDescriptionWhenMissing() {
        BillingTemplateRequest request = new BillingTemplateRequest("tpl-001", "Standard", null, "ACTIVE");

        BillingTemplatePlaceholderResponse response = service.createTemplate("bikini-bottom", "krusty-krab", request);

        assertThat(response.description()).containsKey("summary");
        verify(billingTemplateRepository).save(any(BillingTemplateEntity.class));
    }

    @Test
    void updateTemplateReusesExistingEntity() {
        BillingTemplateEntity existing = BillingTemplateEntity.create("tpl-001", "bikini-bottom", "krusty-krab", "Old", Map.of("summary", "Old"), "INACTIVE");
        when(billingTemplateRepository.findById("tpl-001")).thenReturn(Optional.of(existing));

        BillingTemplatePlaceholderResponse response = service.updateTemplate(
                "bikini-bottom",
                "krusty-krab",
                "tpl-001",
                new BillingTemplateRequest("tpl-001", "Standard", Map.of("summary", "Default"), "ACTIVE")
        );

        assertThat(response.templateName()).isEqualTo("Standard");
        assertThat(existing.getStatus()).isEqualTo("ACTIVE");
        verify(billingTemplateRepository).save(existing);
    }
}
