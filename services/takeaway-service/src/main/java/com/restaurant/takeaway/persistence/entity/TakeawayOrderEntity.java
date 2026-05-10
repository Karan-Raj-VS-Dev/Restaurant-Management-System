package com.restaurant.takeaway.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "takeaway_orders")
public class TakeawayOrderEntity {

    @Id
    @Column(name = "takeaway_order_id", nullable = false, length = 64)
    private String takeawayOrderId;

    @Column(name = "external_order_reference", length = 100)
    private String externalOrderReference;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "property_id", nullable = false, length = 64)
    private String propertyId;

    @Column(name = "customer_id", length = 64)
    private String customerId;

    @Column(name = "customer_name", nullable = false, length = 150)
    private String customerName;

    @Column(name = "customer_phone", length = 32)
    private String customerPhone;

    @Column(name = "fulfillment_type", nullable = false, length = 32)
    private String fulfillmentType;

    @Column(name = "source_channel", nullable = false, length = 32)
    private String sourceChannel;

    @Column(name = "takeaway_status", nullable = false, length = 32)
    private String takeawayStatus;

    @Column(name = "subtotal_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalAmount;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "delivery_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal deliveryFee;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "promised_at")
    private Instant promisedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected TakeawayOrderEntity() {
    }
}
