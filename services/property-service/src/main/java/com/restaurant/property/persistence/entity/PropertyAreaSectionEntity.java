package com.restaurant.property.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "property_area_sections")
public class PropertyAreaSectionEntity {

    @Id
    @Column(name = "area_section_id", nullable = false, length = 64)
    private String areaSectionId;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "property_id", nullable = false, length = 64)
    private String propertyId;

    @Column(name = "floor_name", nullable = false, length = 120)
    private String floorName;

    @Column(name = "section_name", nullable = false, length = 120)
    private String sectionName;

    @Column(name = "max_table_count", nullable = false)
    private Integer maxTableCount;

    @Column(name = "waiter_names", nullable = false)
    private String waiterNames;

    @Column(name = "cleaner_names", nullable = false)
    private String cleanerNames;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public PropertyAreaSectionEntity() {
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public String getAreaSectionId() {
        return areaSectionId;
    }

    public void setAreaSectionId(String areaSectionId) {
        this.areaSectionId = areaSectionId;
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

    public String getFloorName() {
        return floorName;
    }

    public void setFloorName(String floorName) {
        this.floorName = floorName;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public Integer getMaxTableCount() {
        return maxTableCount;
    }

    public void setMaxTableCount(Integer maxTableCount) {
        this.maxTableCount = maxTableCount;
    }

    public String getWaiterNames() {
        return waiterNames;
    }

    public void setWaiterNames(String waiterNames) {
        this.waiterNames = waiterNames;
    }

    public String getCleanerNames() {
        return cleanerNames;
    }

    public void setCleanerNames(String cleanerNames) {
        this.cleanerNames = cleanerNames;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
