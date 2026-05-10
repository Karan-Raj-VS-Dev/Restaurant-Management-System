package com.restaurant.platform.eventing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class EventEnvelopeParser {

    private static final TypeReference<EventEnvelope<JsonNode>> JSON_NODE_ENVELOPE = new TypeReference<>() {
    };

    private EventEnvelopeParser() {
    }

    public static EventEnvelope<JsonNode> parse(String rawMessage, ObjectMapper objectMapper) {
        try {
            return objectMapper.readValue(rawMessage, JSON_NODE_ENVELOPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Failed to parse domain event message", exception);
        }
    }
}
