package com.restaurant.order.persistence.entity;

import com.restaurant.order.OrderStatusTrailEntry;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "order_status_history")
public class OrderStatusHistoryEntity {

    @Id
    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "status_trail", nullable = false, columnDefinition = "jsonb")
    private List<OrderStatusTrailEntry> statusTrail;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public OrderStatusHistoryEntity() {
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (statusTrail == null) {
            statusTrail = new ArrayList<>();
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
        if (statusTrail == null) {
            statusTrail = new ArrayList<>();
        }
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderStatusTrailEntry> getStatusTrail() {
        return statusTrail;
    }

    public void setStatusTrail(List<OrderStatusTrailEntry> statusTrail) {
        this.statusTrail = statusTrail;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
