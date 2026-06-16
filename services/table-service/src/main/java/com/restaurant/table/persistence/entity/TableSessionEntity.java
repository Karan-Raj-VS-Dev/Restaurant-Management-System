package com.restaurant.table.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "table_sessions")
public class TableSessionEntity {

    @Id
    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    @Column(name = "table_id", nullable = false, length = 64)
    private String tableId;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "property_id", nullable = false, length = 64)
    private String propertyId;

    @Column(name = "order_id", length = 64)
    private String orderId;

    @Column(name = "customer_id", length = 64)
    private String customerId;

    @Column(name = "customer_count", nullable = false)
    private Integer customerCount;

    @Column(name = "assigned_waiter_id", length = 64)
    private String assignedWaiterId;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "session_status", nullable = false, length = 32)
    private String sessionStatus;

    public TableSessionEntity() {
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (startedAt == null) {
            startedAt = now;
        }
        if (sessionStatus == null || sessionStatus.isBlank()) {
            sessionStatus = "OPEN";
        }
        if (customerCount == null) {
            customerCount = 0;
        }
    }

    @PreUpdate
    void onUpdate() {
        if (customerCount == null) {
            customerCount = 0;
        }
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
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

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Integer getCustomerCount() {
        return customerCount;
    }

    public void setCustomerCount(Integer customerCount) {
        this.customerCount = customerCount;
    }

    public String getAssignedWaiterId() {
        return assignedWaiterId;
    }

    public void setAssignedWaiterId(String assignedWaiterId) {
        this.assignedWaiterId = assignedWaiterId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }

    public String getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(String sessionStatus) {
        this.sessionStatus = sessionStatus;
    }
}
