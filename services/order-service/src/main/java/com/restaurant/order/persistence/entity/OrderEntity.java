package com.restaurant.order.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
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

    @Column(name = "customer_id", length = 64)
    private String customerId;

    @Column(name = "waiter_id", length = 64)
    private String waiterId;

    @Column(name = "order_type", nullable = false, length = 32)
    private String orderType;

    @Column(name = "order_status", nullable = false, length = 32)
    private String orderStatus;

    @Column(name = "guest_count", nullable = false)
    private Integer guestCount;

    @Column(name = "subtotal_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalAmount;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "special_instructions")
    private String specialInstructions;

    @Column(name = "ordered_at", nullable = false)
    private Instant orderedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected OrderEntity() {
    }
}
