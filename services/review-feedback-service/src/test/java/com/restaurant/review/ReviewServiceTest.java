package com.restaurant.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.restaurant.platform.eventing.DomainEventPublisher;
import com.restaurant.platform.eventing.EventEnvelope;
import com.restaurant.platform.eventing.EventEnvelopeFactory;
import com.restaurant.platform.eventing.contract.PaymentSucceededEvent;
import com.restaurant.platform.eventing.contract.ReviewRequestedEvent;
import com.restaurant.platform.eventing.contract.ReviewSubmittedEvent;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewWorkflowStore reviewWorkflowStore;

    @Mock
    private DomainEventPublisher publisher;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewWorkflowStore, new EventEnvelopeFactory("test-suite"), publisher);
    }

    @Test
    void getReviewRequestMapsStoreRecord() {
        ReviewRequestRecord record = new ReviewRequestRecord("rq-001", "bill-001", "bikini-bottom", "krusty-krab", "cust-001", "PENDING", Instant.parse("2026-06-15T10:00:00Z"));
        when(reviewWorkflowStore.getReviewRequest("bikini-bottom", "krusty-krab", "bill-001")).thenReturn(record);

        ReviewRequestResponse response = reviewService.getReviewRequest("bikini-bottom", "krusty-krab", "bill-001");

        assertThat(response.reviewRequestId()).isEqualTo("rq-001");
        assertThat(response.billId()).isEqualTo("bill-001");
        assertThat(response.status()).isEqualTo("PENDING");
    }

    @Test
    void submitReviewMarksSubmittedAndPublishesEvent() {
        CreateReviewRequest request = new CreateReviewRequest("bill-001", "cust-001", 5, "Great");

        ReviewResponse response = reviewService.submitReview("bikini-bottom", "krusty-krab", request);

        assertThat(response.billId()).isEqualTo("bill-001");
        assertThat(response.customerId()).isEqualTo("cust-001");
        assertThat(response.rating()).isEqualTo(5);
        verify(reviewWorkflowStore).markSubmitted("bikini-bottom", "krusty-krab", "bill-001", "cust-001");

        ArgumentCaptor<EventEnvelope<?>> captor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(publisher).publish(captor.capture());
        assertThat(captor.getValue().causationId()).isNull();
        assertThat(captor.getValue().payload()).isInstanceOfSatisfying(ReviewSubmittedEvent.class, event -> {
            assertThat(event.billId()).isEqualTo("bill-001");
            assertThat(event.customerId()).isEqualTo("cust-001");
            assertThat(event.rating()).isEqualTo(5);
            assertThat(event.tenantId()).isEqualTo("bikini-bottom");
            assertThat(event.propertyId()).isEqualTo("krusty-krab");
        });
    }

    @Test
    void registerReviewRequestFromPaymentPublishesReviewRequest() {
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                "pay-001",
                "bill-001",
                "bikini-bottom",
                "krusty-krab",
                BigDecimal.valueOf(499),
                "CARD",
                Instant.parse("2026-06-15T10:05:00Z")
        );
        ReviewRequestRecord stored = new ReviewRequestRecord("rq-001", "bill-001", "bikini-bottom", "krusty-krab", null, "PENDING", Instant.parse("2026-06-15T10:05:00Z"));
        when(reviewWorkflowStore.registerReviewRequest(event)).thenReturn(stored);

        reviewService.registerReviewRequestFromPayment(event, "cause-001");

        verify(reviewWorkflowStore).registerReviewRequest(event);
        ArgumentCaptor<EventEnvelope<?>> captor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(publisher).publish(captor.capture());
        assertThat(captor.getValue().causationId()).isEqualTo("cause-001");
        assertThat(captor.getValue().payload()).isInstanceOfSatisfying(ReviewRequestedEvent.class, published -> {
            assertThat(published.reviewRequestId()).isEqualTo("rq-001");
            assertThat(published.billId()).isEqualTo("bill-001");
            assertThat(published.tenantId()).isEqualTo("bikini-bottom");
            assertThat(published.propertyId()).isEqualTo("krusty-krab");
            assertThat(published.customerId()).isNull();
        });
    }
}
