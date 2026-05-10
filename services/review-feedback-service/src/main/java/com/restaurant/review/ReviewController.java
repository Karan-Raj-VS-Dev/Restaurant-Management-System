package com.restaurant.review;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({
        "/api/reviews",
        "/chefy/tenant/{tenantId}/property/{propertyId}/api/reviews"
})
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/requests/{billId}")
    public ReviewRequestResponse getReviewRequest(@PathVariable String billId,
                                                  @PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                                  @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                                  @RequestParam(name = "tenantId", required = false) String tenantId,
                                                  @RequestParam(name = "propertyId", required = false) String propertyId) {
        return reviewService.getReviewRequest(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                billId
        );
    }

    @PostMapping
    public ReviewResponse submitReview(@PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                       @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                       @RequestParam(name = "tenantId", required = false) String tenantId,
                                       @RequestParam(name = "propertyId", required = false) String propertyId,
                                       @Valid @RequestBody CreateReviewRequest request) {
        return reviewService.submitReview(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                request
        );
    }

    private String resolveTenantId(String pathTenantId, String requestTenantId) {
        if (pathTenantId != null && !pathTenantId.isBlank()) {
            return pathTenantId;
        }
        if (requestTenantId != null && !requestTenantId.isBlank()) {
            return requestTenantId;
        }
        return "bikini-bottom";
    }

    private String resolvePropertyId(String pathPropertyId, String requestPropertyId) {
        if (pathPropertyId != null && !pathPropertyId.isBlank()) {
            return pathPropertyId;
        }
        if (requestPropertyId != null && !requestPropertyId.isBlank()) {
            return requestPropertyId;
        }
        return "krusty-krab";
    }
}
