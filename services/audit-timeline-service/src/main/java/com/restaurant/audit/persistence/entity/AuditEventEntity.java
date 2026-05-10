package com.restaurant.audit.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "audit_events")
public class AuditEventEntity {

    @Id
    @Column(name = "audit_event_id", nullable = false, length = 64)
    private String auditEventId;

    @Column(name = "event_id", length = 64)
    private String eventId;

    @Column(name = "event_key", nullable = false, length = 128)
    private String eventKey;

    @Column(name = "aggregate_type", nullable = false, length = 64)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 64)
    private String aggregateId;

    @Column(name = "tenant_id", length = 64)
    private String tenantId;

    @Column(name = "property_id", length = 64)
    private String propertyId;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @Column(name = "causation_id", length = 64)
    private String causationId;

    @Column(name = "producer", nullable = false, length = 128)
    private String producer;

    @Column(name = "actor_id", length = 64)
    private String actorId;

    @Column(name = "actor_type", length = 64)
    private String actorType;

    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    protected AuditEventEntity() {
    }
}
