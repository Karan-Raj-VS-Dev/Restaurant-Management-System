package com.restaurant.billing;

import com.restaurant.billing.persistence.repository.BillingTemplateRepository;
import com.restaurant.billing.persistence.repository.TaxDefinitionRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class BillingSettingsService {

    private final TaxDefinitionRepository taxDefinitionRepository;
    private final BillingTemplateRepository billingTemplateRepository;

    public BillingSettingsService(TaxDefinitionRepository taxDefinitionRepository,
                                  BillingTemplateRepository billingTemplateRepository) {
        this.taxDefinitionRepository = taxDefinitionRepository;
        this.billingTemplateRepository = billingTemplateRepository;
    }

    public BillingSettingsSummaryResponse getSummary(String tenantId, String propertyId) {
        return new BillingSettingsSummaryResponse(
                taxDefinitionRepository.findByTenantIdAndPropertyIdOrderByTaxNameAsc(tenantId, propertyId).stream()
                        .map(tax -> new TaxSettingResponse(
                                tax.getTaxId(),
                                tax.getTaxName(),
                                tax.getRatePercent(),
                                tax.getStatus(),
                                tax.getAppliesTo()
                        ))
                        .toList(),
                billingTemplateRepository.findByTenantIdAndPropertyIdOrderByTemplateNameAsc(tenantId, propertyId).stream()
                        .map(template -> new BillingTemplatePlaceholderResponse(
                                template.getTemplateId(),
                                template.getTemplateName(),
                                template.getDescription(),
                                template.getStatus()
                        ))
                        .toList()
        );
    }

    public TaxSettingResponse createTax(String tenantId, String propertyId, TaxSettingRequest request) {
        var entity = com.restaurant.billing.persistence.entity.TaxDefinitionEntity.create(
                request.taxId(),
                tenantId,
                propertyId,
                request.taxName(),
                request.ratePercent(),
                request.appliesTo(),
                request.status()
        );
        taxDefinitionRepository.save(entity);
        return new TaxSettingResponse(entity.getTaxId(), entity.getTaxName(), entity.getRatePercent(), entity.getStatus(), entity.getAppliesTo());
    }

    public TaxSettingResponse updateTax(String tenantId, String propertyId, String taxId, TaxSettingRequest request) {
        var entity = taxDefinitionRepository.findById(taxId)
                .orElseGet(() -> com.restaurant.billing.persistence.entity.TaxDefinitionEntity.create(
                        taxId,
                        tenantId,
                        propertyId,
                        request.taxName(),
                        request.ratePercent(),
                        request.appliesTo(),
                        request.status()
                ));
        entity.update(request.taxName(), request.ratePercent(), request.appliesTo(), request.status());
        taxDefinitionRepository.save(entity);
        return new TaxSettingResponse(entity.getTaxId(), entity.getTaxName(), entity.getRatePercent(), entity.getStatus(), entity.getAppliesTo());
    }

    public BillingTemplatePlaceholderResponse createTemplate(String tenantId, String propertyId, BillingTemplateRequest request) {
        var entity = com.restaurant.billing.persistence.entity.BillingTemplateEntity.create(
                request.templateId(),
                tenantId,
                propertyId,
                request.templateName(),
                safeDescription(request.description()),
                request.status()
        );
        billingTemplateRepository.save(entity);
        return new BillingTemplatePlaceholderResponse(entity.getTemplateId(), entity.getTemplateName(), entity.getDescription(), entity.getStatus());
    }

    public BillingTemplatePlaceholderResponse updateTemplate(String tenantId, String propertyId, String templateId, BillingTemplateRequest request) {
        var entity = billingTemplateRepository.findById(templateId)
                .orElseGet(() -> com.restaurant.billing.persistence.entity.BillingTemplateEntity.create(
                        templateId,
                        tenantId,
                        propertyId,
                        request.templateName(),
                        safeDescription(request.description()),
                        request.status()
                ));
        entity.update(request.templateName(), safeDescription(request.description()), request.status());
        billingTemplateRepository.save(entity);
        return new BillingTemplatePlaceholderResponse(entity.getTemplateId(), entity.getTemplateName(), entity.getDescription(), entity.getStatus());
    }

    private Map<String, Object> safeDescription(Map<String, Object> description) {
        if (description == null || description.isEmpty()) {
            return Map.of(
                    "summary", "Structured bill-template definition placeholder is ready.",
                    "sections", List.of()
            );
        }
        return description;
    }
}
