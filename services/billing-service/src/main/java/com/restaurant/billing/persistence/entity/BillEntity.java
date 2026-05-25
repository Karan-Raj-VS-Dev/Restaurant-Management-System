package com.restaurant.billing.persistence.entity;

import com.restaurant.billing.BillSettlementType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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

    @Column(name = "linked_order_ids", nullable = false)
    private String linkedOrderIds;

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

    @Column(name = "settlement_type", nullable = false, length = 32)
    private String settlementType;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "cancellation_fee_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal cancellationFeeAmount;

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

    public BillEntity() {
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (generatedAt == null) {
            generatedAt = now;
        }
        if (linkedOrderIds == null) {
            linkedOrderIds = orderId == null ? "" : orderId;
        }
        if (settlementType == null) {
            settlementType = BillSettlementType.STANDARD.name();
        }
        if (cancellationFeeAmount == null) {
            cancellationFeeAmount = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    void onUpdate() {
        if (linkedOrderIds == null) {
            linkedOrderIds = orderId == null ? "" : orderId;
        }
        if (settlementType == null) {
            settlementType = BillSettlementType.STANDARD.name();
        }
        if (cancellationFeeAmount == null) {
            cancellationFeeAmount = BigDecimal.ZERO;
        }
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getLinkedOrderIds() {
        return linkedOrderIds;
    }

    public void setLinkedOrderIds(String linkedOrderIds) {
        this.linkedOrderIds = linkedOrderIds;
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

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getBillingStatus() {
        return billingStatus;
    }

    public void setBillingStatus(String billingStatus) {
        this.billingStatus = billingStatus;
    }

    public String getSettlementType() {
        return settlementType;
    }

    public void setSettlementType(String settlementType) {
        this.settlementType = settlementType;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public BigDecimal getCancellationFeeAmount() {
        return cancellationFeeAmount;
    }

    public void setCancellationFeeAmount(BigDecimal cancellationFeeAmount) {
        this.cancellationFeeAmount = cancellationFeeAmount;
    }

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    public void setSubtotalAmount(BigDecimal subtotalAmount) {
        this.subtotalAmount = subtotalAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getServiceChargeAmount() {
        return serviceChargeAmount;
    }

    public void setServiceChargeAmount(BigDecimal serviceChargeAmount) {
        this.serviceChargeAmount = serviceChargeAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }
}
