package com.restaurant.kitchen.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "kitchen_tickets")
public class KitchenTicketEntity {

    @Id
    @Column(name = "ticket_id", nullable = false, length = 64)
    private String ticketId;

    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "property_id", nullable = false, length = 64)
    private String propertyId;

    @Column(name = "table_id", length = 64)
    private String tableId;

    @Column(name = "ticket_status", nullable = false, length = 32)
    private String ticketStatus;

    @Column(name = "assigned_cook_id", length = 64)
    private String assignedCookId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "ready_at")
    private Instant readyAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected KitchenTicketEntity() {
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    @PreUpdate
    void onUpdate() {
        if (readyAt == null && "READY".equals(ticketStatus)) {
            readyAt = Instant.now();
        }
        if (completedAt == null && "SERVED".equals(ticketStatus)) {
            completedAt = Instant.now();
        }
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
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

    public String getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(String ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

    public String getAssignedCookId() {
        return assignedCookId;
    }

    public void setAssignedCookId(String assignedCookId) {
        this.assignedCookId = assignedCookId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Instant acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public Instant getReadyAt() {
        return readyAt;
    }

    public void setReadyAt(Instant readyAt) {
        this.readyAt = readyAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public static KitchenTicketEntity create(String ticketId,
                                             String orderId,
                                             String tenantId,
                                             String propertyId,
                                             String tableId,
                                             String ticketStatus,
                                             String assignedCookId,
                                             Instant createdAt) {
        KitchenTicketEntity entity = new KitchenTicketEntity();
        entity.setTicketId(ticketId);
        entity.setOrderId(orderId);
        entity.setTenantId(tenantId);
        entity.setPropertyId(propertyId);
        entity.setTableId(tableId);
        entity.setTicketStatus(ticketStatus);
        entity.setAssignedCookId(assignedCookId);
        entity.setCreatedAt(createdAt);
        return entity;
    }
}
