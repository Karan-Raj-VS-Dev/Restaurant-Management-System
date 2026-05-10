package com.restaurant.platform.eventing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

public class HttpDomainEventPublisher implements DomainEventPublisher {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String gatewayUrl;

    public HttpDomainEventPublisher(RestClient restClient,
                                    ObjectMapper objectMapper,
                                    String gatewayUrl) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.gatewayUrl = gatewayUrl;
    }

    @Override
    public void publish(EventEnvelope<?> eventEnvelope) {
        try {
            restClient.post()
                    .uri(gatewayUrl + "/api/events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(eventEnvelope))
                    .retrieve()
                    .toBodilessEntity();
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize event " + eventEnvelope.eventKey(), exception);
        }
    }
}
