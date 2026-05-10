package com.restaurant.review;

import com.restaurant.platform.eventing.contract.PaymentSucceededEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ReviewWorkflowStore {

    private final Map<String, ReviewRequestRecord> reviewRequestsByBillId = new ConcurrentHashMap<>();

    public ReviewRequestRecord registerReviewRequest(PaymentSucceededEvent event) {
        ReviewRequestRecord record = new ReviewRequestRecord(
                "rq-" + UUID.randomUUID(),
                event.billId(),
                event.tenantId(),
                event.propertyId(),
                null,
                "PENDING",
                Instant.now()
        );
        reviewRequestsByBillId.put(scopeKey(event.tenantId(), event.propertyId(), event.billId()), record);
        return record;
    }

    public ReviewRequestRecord getReviewRequest(String tenantId, String propertyId, String billId) {
        return reviewRequestsByBillId.getOrDefault(
                scopeKey(tenantId, propertyId, billId),
                new ReviewRequestRecord("rq-missing", billId, tenantId, propertyId, null, "NOT_FOUND", Instant.now())
        );
    }

    public void markSubmitted(String tenantId, String propertyId, String billId, String customerId) {
        reviewRequestsByBillId.computeIfPresent(scopeKey(tenantId, propertyId, billId), (ignored, request) -> new ReviewRequestRecord(
                request.reviewRequestId(),
                request.billId(),
                request.tenantId(),
                request.propertyId(),
                customerId,
                "SUBMITTED",
                request.requestedAt()
        ));
    }

    private String scopeKey(String tenantId, String propertyId, String billId) {
        return tenantId + "::" + propertyId + "::" + billId;
    }

}
