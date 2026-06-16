package com.restaurant.billing.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "bill_orders")
@IdClass(BillOrderId.class)
public class BillOrderEntity {

    @Id
    @Column(name = "bill_id", nullable = false, length = 64)
    private String billId;

    @Id
    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;

    @Column(name = "attached_at", nullable = false)
    private Instant attachedAt;

    public BillOrderEntity() {
    }

    @PrePersist
    void onCreate() {
        if (attachedAt == null) {
            attachedAt = Instant.now();
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

    public Instant getAttachedAt() {
        return attachedAt;
    }

    public void setAttachedAt(Instant attachedAt) {
        this.attachedAt = attachedAt;
    }
}
