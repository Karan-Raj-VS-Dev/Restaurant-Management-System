package com.restaurant.review;

import com.restaurant.platform.eventing.AggregateTypes;
import com.restaurant.platform.eventing.DomainEventPublisher;
import com.restaurant.platform.eventing.EventEnvelopeFactory;
import com.restaurant.platform.eventing.EventKeys;
import com.restaurant.platform.eventing.contract.PaymentSucceededEvent;
import com.restaurant.platform.eventing.contract.ReviewRequestedEvent;
import com.restaurant.platform.eventing.contract.ReviewSubmittedEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class ReviewService {

    private final ReviewWorkflowStore reviewWorkflowStore;
    private final EventEnvelopeFactory eventEnvelopeFactory;
    private final DomainEventPublisher domainEventPublisher;

    public ReviewService(ReviewWorkflowStore reviewWorkflowStore,
                         EventEnvelopeFactory eventEnvelopeFactory,
                         DomainEventPublisher domainEventPublisher) {
        this.reviewWorkflowStore = reviewWorkflowStore;
        this.eventEnvelopeFactory = eventEnvelopeFactory;
        this.domainEventPublisher = domainEventPublisher;
    }

    public ReviewRequestResponse getReviewRequest(String tenantId, String propertyId, String billId) {
        ReviewRequestRecord request = reviewWorkflowStore.getReviewRequest(tenantId, propertyId, billId);
        return new ReviewRequestResponse(
                request.reviewRequestId(),
                request.billId(),
                request.tenantId(),
                request.propertyId(),
                request.status(),
                request.requestedAt()
        );
    }

    public ReviewResponse submitReview(String tenantId, String propertyId, CreateReviewRequest request) {
        String reviewId = "rev-" + UUID.randomUUID();
        reviewWorkflowStore.markSubmitted(tenantId, propertyId, request.billId(), request.customerId());
        ReviewResponse response = new ReviewResponse(
                reviewId,
                request.billId(),
                tenantId,
                propertyId,
                request.customerId(),
                request.rating(),
                request.comments(),
                Instant.now()
        );
        domainEventPublisher.publish(eventEnvelopeFactory.create(
                EventKeys.REVIEW_SUBMITTED,
                AggregateTypes.REVIEW,
                response.reviewId(),
                response.billId(),
                response.billId(),
                null,
                new ReviewSubmittedEvent(
                        response.reviewId(),
                        response.billId(),
                        response.tenantId(),
                        response.propertyId(),
                        response.customerId(),
                        response.rating(),
                        response.createdAt()
                )
        ));
        return response;
    }

    public void registerReviewRequestFromPayment(PaymentSucceededEvent event, String causationId) {
        ReviewRequestRecord reviewRequest = reviewWorkflowStore.registerReviewRequest(event);
        domainEventPublisher.publish(eventEnvelopeFactory.create(
                EventKeys.REVIEW_REQUESTED,
                AggregateTypes.REVIEW,
                reviewRequest.reviewRequestId(),
                reviewRequest.billId(),
                reviewRequest.billId(),
                causationId,
                new ReviewRequestedEvent(
                        reviewRequest.reviewRequestId(),
                        reviewRequest.billId(),
                        reviewRequest.tenantId(),
                        reviewRequest.propertyId(),
                        reviewRequest.customerId(),
                        reviewRequest.requestedAt()
                )
        ));
    }
}
