package com.restaurant.reporting.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "daily_property_summary")
public class DailyPropertySummaryEntity {

    @Id
    @Column(name = "summary_id", nullable = false, length = 64)
    private String summaryId;

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

    @Column(name = "total_revenue", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalRevenue;

    @Column(name = "total_tax", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalTax;

    @Column(name = "total_discounts", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalDiscounts;

    @Column(name = "average_ticket_size", nullable = false, precision = 14, scale = 2)
    private BigDecimal averageTicketSize;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    protected DailyPropertySummaryEntity() {
    }
}
