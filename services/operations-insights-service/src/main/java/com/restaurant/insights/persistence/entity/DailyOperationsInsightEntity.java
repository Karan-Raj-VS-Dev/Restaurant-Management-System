package com.restaurant.insights.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "daily_operations_insights")
public class DailyOperationsInsightEntity {

    @Id
    @Column(name = "insight_id", nullable = false, length = 64)
    private String insightId;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "property_id", nullable = false, length = 64)
    private String propertyId;

    @Column(name = "business_date", nullable = false)
    private LocalDate businessDate;

    @Column(name = "total_orders", nullable = false)
    private Integer totalOrders;

    @Column(name = "dine_in_orders", nullable = false)
    private Integer dineInOrders;

    @Column(name = "takeaway_orders", nullable = false)
    private Integer takeawayOrders;

    @Column(name = "completed_payments", nullable = false)
    private Integer completedPayments;

    @Column(name = "busiest_table_id", length = 64)
    private String busiestTableId;

    @Column(name = "busiest_table_customer_count", nullable = false)
    private Integer busiestTableCustomerCount;

    @Column(name = "top_server_id", length = 64)
    private String topServerId;

    @Column(name = "top_server_customer_count", nullable = false)
    private Integer topServerCustomerCount;

    @Column(name = "kitchen_active_tickets", nullable = false)
    private Integer kitchenActiveTickets;

    @Column(name = "total_revenue", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalRevenue;

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt;

    protected DailyOperationsInsightEntity() {
    }
}
