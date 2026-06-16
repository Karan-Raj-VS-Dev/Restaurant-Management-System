package com.restaurant.customer.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "customers")
public class CustomerEntity {

    @Id
    @Column(name = "customer_id", nullable = false, length = 64)
    private String customerId;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "property_id", length = 64)
    private String propertyId;

    @Column(name = "full_name", length = 150)
    private String fullName;

    @Column(name = "phone_number", length = 32)
    private String phoneNumber;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CustomerEntity() {
    }

    public static CustomerEntity create(String customerId,
                                        String tenantId,
                                        String propertyId,
                                        String fullName,
                                        String phoneNumber) {
        CustomerEntity entity = new CustomerEntity();
        entity.customerId = customerId;
        entity.tenantId = tenantId;
        entity.propertyId = propertyId;
        entity.fullName = fullName;
        entity.phoneNumber = phoneNumber;
        Instant now = Instant.now();
        entity.createdAt = now;
        entity.updatedAt = now;
        return entity;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
