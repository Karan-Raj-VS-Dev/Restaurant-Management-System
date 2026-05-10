package com.restaurant.billing.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "tax_definitions")
public class TaxDefinitionEntity {

    @Id
    @Column(name = "tax_id", nullable = false, length = 64)
    private String taxId;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "property_id", nullable = false, length = 64)
    private String propertyId;

    @Column(name = "tax_name", nullable = false, length = 150)
    private String taxName;

    @Column(name = "rate_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal ratePercent;

    @Column(name = "applies_to", nullable = false, length = 64)
    private String appliesTo;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected TaxDefinitionEntity() {
    }

    public String getTaxId() {
        return taxId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public String getTaxName() {
        return taxName;
    }

    public BigDecimal getRatePercent() {
        return ratePercent;
    }

    public String getAppliesTo() {
        return appliesTo;
    }

    public String getStatus() {
        return status;
    }

    public static TaxDefinitionEntity create(String taxId,
                                             String tenantId,
                                             String propertyId,
                                             String taxName,
                                             BigDecimal ratePercent,
                                             String appliesTo,
                                             String status) {
        TaxDefinitionEntity entity = new TaxDefinitionEntity();
        entity.taxId = taxId;
        entity.tenantId = tenantId;
        entity.propertyId = propertyId;
        entity.taxName = taxName;
        entity.ratePercent = ratePercent;
        entity.appliesTo = appliesTo;
        entity.status = status;
        entity.createdAt = Instant.now();
        entity.updatedAt = entity.createdAt;
        return entity;
    }

    public void update(String taxName, BigDecimal ratePercent, String appliesTo, String status) {
        this.taxName = taxName;
        this.ratePercent = ratePercent;
        this.appliesTo = appliesTo;
        this.status = status;
        this.updatedAt = Instant.now();
    }
}
