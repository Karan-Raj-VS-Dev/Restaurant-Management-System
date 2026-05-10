package com.restaurant.billing.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "billing_templates")
public class BillingTemplateEntity {

    @Id
    @Column(name = "template_id", nullable = false, length = 64)
    private String templateId;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "property_id", nullable = false, length = 64)
    private String propertyId;

    @Column(name = "template_name", nullable = false, length = 150)
    private String templateName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "description", columnDefinition = "jsonb")
    private Map<String, Object> description;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected BillingTemplateEntity() {
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public Map<String, Object> getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public static BillingTemplateEntity create(String templateId,
                                               String tenantId,
                                               String propertyId,
                                               String templateName,
                                               Map<String, Object> description,
                                               String status) {
        BillingTemplateEntity entity = new BillingTemplateEntity();
        entity.templateId = templateId;
        entity.tenantId = tenantId;
        entity.propertyId = propertyId;
        entity.templateName = templateName;
        entity.description = description;
        entity.status = status;
        entity.createdAt = Instant.now();
        entity.updatedAt = entity.createdAt;
        return entity;
    }

    public void update(String templateName, Map<String, Object> description, String status) {
        this.templateName = templateName;
        this.description = description;
        this.status = status;
        this.updatedAt = Instant.now();
    }
}
