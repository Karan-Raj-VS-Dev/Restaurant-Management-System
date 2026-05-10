package com.restaurant.audit.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.audit.AuditTimelineService;
import com.restaurant.platform.eventing.DomainEventHandler;
import com.restaurant.platform.eventing.EventEnvelope;
import com.restaurant.platform.eventing.EventEnvelopeParser;
import org.springframework.stereotype.Component;

@Component
public class AuditDomainEventListener implements DomainEventHandler {

    private final ObjectMapper objectMapper;
    private final AuditTimelineService auditTimelineService;

    public AuditDomainEventListener(ObjectMapper objectMapper,
                                    AuditTimelineService auditTimelineService) {
        this.objectMapper = objectMapper;
        this.auditTimelineService = auditTimelineService;
    }

    @Override
    public void handle(String rawMessage) {
        EventEnvelope<com.fasterxml.jackson.databind.JsonNode> envelope = EventEnvelopeParser.parse(rawMessage, objectMapper);
        auditTimelineService.recordEvent(envelope);
    }
}
