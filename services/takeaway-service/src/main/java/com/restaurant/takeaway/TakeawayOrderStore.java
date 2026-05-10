package com.restaurant.takeaway;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TakeawayOrderStore {

    private final Map<String, TakeawayOrderRecord> takeawayOrders = new ConcurrentHashMap<>();

    public TakeawayOrderRecord createOrder(String tenantId, String propertyId, String channel, String sourceReferenceId) {
        TakeawayOrderRecord record = new TakeawayOrderRecord(
                "to-" + UUID.randomUUID(),
                tenantId,
                propertyId,
                channel,
                sourceReferenceId,
                "CREATED",
                Instant.now()
        );
        takeawayOrders.put(scopeKey(tenantId, propertyId, record.takeawayOrderId()), record);
        return record;
    }

    public TakeawayOrderRecord find(String tenantId, String propertyId, String takeawayOrderId) {
        return takeawayOrders.getOrDefault(
                scopeKey(tenantId, propertyId, takeawayOrderId),
                new TakeawayOrderRecord(takeawayOrderId, tenantId, propertyId, "DIRECT", null, "PREPARING", Instant.now())
        );
    }

    private String scopeKey(String tenantId, String propertyId, String takeawayOrderId) {
        return tenantId + "::" + propertyId + "::" + takeawayOrderId;
    }

}
