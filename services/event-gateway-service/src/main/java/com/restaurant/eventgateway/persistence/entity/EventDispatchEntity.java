package com.restaurant.eventgateway.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "event_dispatches")
public class EventDispatchEntity {

    @Id
    @Column(name = "dispatch_id", nullable = false, length = 64)
    private String dispatchId;

    @Column(name = "event_id", nullable = false, length = 64)
    private String eventId;

    @Column(name = "event_key", nullable = false, length = 128)
    private String eventKey;

    @Column(name = "subscriber_name", nullable = false, length = 128)
    private String subscriberName;

    @Column(name = "endpoint_url", nullable = false)
    private String endpointUrl;

    @Column(name = "dispatch_status", nullable = false, length = 32)
    private String dispatchStatus;

    @Column(name = "response_status_code")
    private Integer responseStatusCode;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "dispatched_at", nullable = false)
    private Instant dispatchedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected EventDispatchEntity() {
    }
}
