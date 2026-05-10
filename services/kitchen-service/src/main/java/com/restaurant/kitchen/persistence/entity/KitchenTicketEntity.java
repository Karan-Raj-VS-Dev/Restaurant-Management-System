package com.restaurant.kitchen.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

    @Column(name = "station_name", length = 64)
    private String stationName;

    @Column(name = "priority_level", nullable = false, length = 32)
    private String priorityLevel;

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
}
