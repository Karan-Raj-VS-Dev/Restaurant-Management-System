package com.restaurant.audit;

import com.restaurant.platform.eventing.EventEnvelope;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class AuditTimelineService {

    private final AuditTimelineStore auditTimelineStore;

    public AuditTimelineService(AuditTimelineStore auditTimelineStore) {
        this.auditTimelineStore = auditTimelineStore;
    }

    @PostConstruct
    void initialize() {
        auditTimelineStore.append(new AuditEntry(
                "bootstrap-1",
                "system",
                "bikini-bottom",
                "krusty-krab",
                "system.bootstrap",
                "Audit timeline initialized",
                "audit-timeline-service",
                Instant.now()
        ));
    }

    public List<AuditEventResponse> listEvents(String tenantId, String propertyId, String referenceId) {
        return auditTimelineStore.findByScope(tenantId, propertyId, referenceId).stream()
                .map(entry -> new AuditEventResponse(
                        entry.referenceId(),
                        entry.tenantId(),
                        entry.propertyId(),
                        entry.eventKey(),
                        entry.message(),
                        entry.occurredAt()
                ))
                .toList();
    }

    public void recordEvent(EventEnvelope<?> envelope) {
        String tenantId = "bikini-bottom";
        String propertyId = "krusty-krab";
        if (envelope.payload() instanceof com.fasterxml.jackson.databind.JsonNode jsonNode) {
            tenantId = readField(jsonNode, "tenantId", tenantId);
            propertyId = readField(jsonNode, "propertyId", propertyId);
        }
        auditTimelineStore.append(new AuditEntry(
                envelope.eventId(),
                envelope.aggregateId(),
                tenantId,
                propertyId,
                envelope.eventKey(),
                "Event " + envelope.eventKey() + " received from " + envelope.producer(),
                envelope.producer(),
                envelope.occurredAt()
        ));
    }

    private String readField(com.fasterxml.jackson.databind.JsonNode node, String fieldName, String fallback) {
        com.fasterxml.jackson.databind.JsonNode value = node.get(fieldName);
        if (value != null && !value.isNull() && !value.asText().isBlank()) {
            return value.asText();
        }
        return fallback;
    }
}
