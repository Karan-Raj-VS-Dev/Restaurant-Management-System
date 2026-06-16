package com.restaurant.audit;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.platform.eventing.EventEnvelope;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class AuditTimelineServiceTest {

    @Test
    void initializeAppendsBootstrapEvent() {
        AuditTimelineStore store = new AuditTimelineStore();
        AuditTimelineService service = new AuditTimelineService(store);

        service.initialize();

        List<AuditEventResponse> responses = service.listEvents("bikini-bottom", "krusty-krab", null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).eventType()).isEqualTo("system.bootstrap");
        assertThat(responses.get(0).message()).contains("initialized");
    }

    @Test
    void recordEventUsesPayloadScopeWhenPresent() throws Exception {
        AuditTimelineStore store = new AuditTimelineStore();
        AuditTimelineService service = new AuditTimelineService(store);
        ObjectMapper objectMapper = new ObjectMapper();

        EventEnvelope<com.fasterxml.jackson.databind.JsonNode> envelope = new EventEnvelope<>(
                "evt-001",
                "order.created",
                "order",
                "order-001",
                "krusty-krab",
                "order-service",
                Instant.parse("2026-06-15T06:00:00Z"),
                "corr-001",
                null,
                objectMapper.readTree("{\"tenantId\":\"tenant-01\",\"propertyId\":\"property-01\"}")
        );

        service.recordEvent(envelope);

        List<AuditEventResponse> responses = service.listEvents("tenant-01", "property-01", "order-001");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).referenceId()).isEqualTo("order-001");
        assertThat(responses.get(0).eventType()).isEqualTo("order.created");
        assertThat(responses.get(0).message()).contains("received from order-service");
    }

    @Test
    void recordEventFallsBackToDefaultScopeWhenPayloadFieldsAreBlank() throws Exception {
        AuditTimelineStore store = new AuditTimelineStore();
        AuditTimelineService service = new AuditTimelineService(store);
        ObjectMapper objectMapper = new ObjectMapper();

        EventEnvelope<com.fasterxml.jackson.databind.JsonNode> envelope = new EventEnvelope<>(
                "evt-blank",
                "order.updated",
                "order",
                "order-blank",
                "krusty-krab",
                "order-service",
                Instant.parse("2026-06-15T06:10:00Z"),
                "corr-blank",
                null,
                objectMapper.readTree("{\"tenantId\":\"   \",\"propertyId\":\"\"}")
        );

        service.recordEvent(envelope);

        List<AuditEventResponse> responses = service.listEvents("bikini-bottom", "krusty-krab", "order-blank");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).eventType()).isEqualTo("order.updated");
    }
}
