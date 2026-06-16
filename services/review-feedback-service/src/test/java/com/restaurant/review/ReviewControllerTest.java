package com.restaurant.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    private ReviewController controller;

    @BeforeEach
    void setUp() {
        controller = new ReviewController(reviewService);
    }

    @Test
    void getReviewRequestPrefersPathScope() {
        ReviewRequestResponse expected = new ReviewRequestResponse("rq-001", "bill-001", "tenant-path", "property-path", "PENDING", Instant.parse("2026-06-15T10:00:00Z"));
        when(reviewService.getReviewRequest("tenant-path", "property-path", "bill-001")).thenReturn(expected);

        ReviewRequestResponse response = controller.getReviewRequest(
                "bill-001",
                "tenant-path",
                "property-path",
                "tenant-query",
                "property-query"
        );

        assertThat(response).isEqualTo(expected);
        verify(reviewService).getReviewRequest("tenant-path", "property-path", "bill-001");
    }

    @Test
    void submitReviewFallsBackToDefaults() {
        CreateReviewRequest request = new CreateReviewRequest("bill-001", "cust-001", 5, "Great");
        ReviewResponse expected = new ReviewResponse("rev-001", "bill-001", "bikini-bottom", "krusty-krab", "cust-001", 5, "Great", Instant.parse("2026-06-15T10:05:00Z"));
        when(reviewService.submitReview("bikini-bottom", "krusty-krab", request)).thenReturn(expected);

        ReviewResponse response = controller.submitReview(null, null, null, null, request);

        assertThat(response).isEqualTo(expected);
        verify(reviewService).submitReview("bikini-bottom", "krusty-krab", request);
    }
}
