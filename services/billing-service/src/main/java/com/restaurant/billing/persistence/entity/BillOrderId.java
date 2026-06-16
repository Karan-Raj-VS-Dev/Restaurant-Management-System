package com.restaurant.billing.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

public class BillOrderId implements Serializable {

    private String billId;
    private String orderId;

    public BillOrderId() {
    }

    public BillOrderId(String billId, String orderId) {
        this.billId = billId;
        this.orderId = orderId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof BillOrderId that)) {
            return false;
        }
        return Objects.equals(billId, that.billId) && Objects.equals(orderId, that.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(billId, orderId);
    }
}
