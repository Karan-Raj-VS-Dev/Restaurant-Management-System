package com.restaurant.insights.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "stock_insights")
public class StockInsightEntity {

    @Id
    @Column(name = "stock_insight_id", nullable = false, length = 64)
    private String stockInsightId;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "property_id", nullable = false, length = 64)
    private String propertyId;

    @Column(name = "business_date", nullable = false)
    private LocalDate businessDate;

    @Column(name = "ingredient_id", nullable = false, length = 64)
    private String ingredientId;

    @Column(name = "ingredient_name", nullable = false, length = 150)
    private String ingredientName;

    @Column(name = "current_quantity", nullable = false, precision = 14, scale = 3)
    private BigDecimal currentQuantity;

    @Column(name = "reserved_quantity", nullable = false, precision = 14, scale = 3)
    private BigDecimal reservedQuantity;

    @Column(name = "available_quantity", nullable = false, precision = 14, scale = 3)
    private BigDecimal availableQuantity;

    @Column(name = "reorder_level", nullable = false, precision = 14, scale = 3)
    private BigDecimal reorderLevel;

    @Column(name = "low_stock", nullable = false)
    private boolean lowStock;

    @Column(name = "stock_out", nullable = false)
    private boolean stockOut;

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt;

    protected StockInsightEntity() {
    }
}
