package com.restaurant.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateReviewRequest(
        @NotBlank String billId,
        String customerId,
        @Min(1) @Max(5) int rating,
        String comments
) {
}
