package com.restaurant.review.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "reviews")
public class ReviewEntity {

    @Id
    @Column(name = "review_id", nullable = false, length = 64)
    private String reviewId;

    @Column(name = "review_request_id", length = 64)
    private String reviewRequestId;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "property_id", nullable = false, length = 64)
    private String propertyId;

    @Column(name = "customer_id", length = 64)
    private String customerId;

    @Column(name = "overall_rating", nullable = false)
    private Short overallRating;

    @Column(name = "food_rating")
    private Short foodRating;

    @Column(name = "service_rating")
    private Short serviceRating;

    @Column(name = "ambiance_rating")
    private Short ambianceRating;

    @Column(name = "comments")
    private String comments;

    @Column(name = "review_status", nullable = false, length = 32)
    private String reviewStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ReviewEntity() {
    }
}
