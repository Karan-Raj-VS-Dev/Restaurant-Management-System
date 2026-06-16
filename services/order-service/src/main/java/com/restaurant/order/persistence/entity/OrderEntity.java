package com.restaurant.order.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "property_id", nullable = false, length = 64)
    private String propertyId;

    @Column(name = "table_id", length = 64)
    private String tableId;

    @Column(name = "session_id", length = 64)
    private String sessionId;

    @Column(name = "waiter_id", length = 64)
    private String waiterId;

    @Column(name = "order_type", nullable = false, length = 32)
    private String orderType;

    @Column(name = "order_status", nullable = false, length = 32)
    private String orderStatus;

    @Column(name = "special_instructions")
    private String specialInstructions;

    @Column(name = "served_at")
    private Instant servedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "ordered_at", nullable = false)
    private Instant orderedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public OrderEntity() {
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (orderedAt == null) {
            orderedAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getWaiterId() {
        return waiterId;
    }

    public void setWaiterId(String waiterId) {
        this.waiterId = waiterId;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public Instant getOrderedAt() {
        return orderedAt;
    }

    public void setOrderedAt(Instant orderedAt) {
        this.orderedAt = orderedAt;
    }

    public Instant getServedAt() {
        return servedAt;
    }

    public void setServedAt(Instant servedAt) {
        this.servedAt = servedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
