package com.restaurant.billing.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "bills")
public class BillEntity {

    @Id
    @Column(name = "bill_id", nullable = false, length = 64)
    private String billId;

    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "property_id", nullable = false, length = 64)
    private String propertyId;

    @Column(name = "table_id", length = 64)
    private String tableId;

    @Column(name = "customer_id", length = 64)
    private String customerId;

    @Column(name = "billing_status", nullable = false, length = 32)
    private String billingStatus;

    @Column(name = "subtotal_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalAmount;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "service_charge_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal serviceChargeAmount;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    protected BillEntity() {
    }
}
