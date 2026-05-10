package com.restaurant.platform.eventing;

public final class EventKeys {

    public static final String TABLE_ASSIGNED = "table.assigned.v1";
    public static final String TABLE_STATUS_CHANGED = "table.status-changed.v1";
    public static final String ORDER_CREATED = "order.created.v1";
    public static final String ORDER_SUBMITTED_TO_KITCHEN = "order.submitted-to-kitchen.v1";
    public static final String KITCHEN_TICKET_CREATED = "kitchen.ticket-created.v1";
    public static final String KITCHEN_STATUS_UPDATED = "kitchen.status-updated.v1";
    public static final String BILL_DRAFTED = "bill.drafted.v1";
    public static final String BILL_FINALIZED = "bill.finalized.v1";
    public static final String PAYMENT_SUCCEEDED = "payment.succeeded.v1";
    public static final String PAYMENT_FAILED = "payment.failed.v1";
    public static final String REVIEW_REQUESTED = "review.requested.v1";
    public static final String REVIEW_SUBMITTED = "review.submitted.v1";
    public static final String STOCK_RESERVED = "stock.reserved.v1";
    public static final String STOCK_LOW = "stock.low.v1";
    public static final String STOCK_OUT = "stock.out.v1";
    public static final String MARKETPLACE_ORDER_RECEIVED = "marketplace.order-received.v1";
    public static final String TAKEAWAY_ORDER_CREATED = "takeaway.order-created.v1";

    private EventKeys() {
    }
}
