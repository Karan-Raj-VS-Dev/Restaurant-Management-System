package com.restaurant.eventgateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

@Service
public class EventDispatchService {

    private final RestClient restClient;
    private final List<String> subscribers;

    public EventDispatchService(@Value("${app.eventing.subscribers:}") String subscribersProperty) {
        this.restClient = RestClient.builder().build();
        this.subscribers = Arrays.stream(subscribersProperty.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    public EventDispatchResponse publish(String rawEvent) {
        int delivered = 0;
        int failed = 0;

        for (String subscriber : subscribers) {
            try {
                restClient.post()
                        .uri(subscriber)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(rawEvent)
                        .retrieve()
                        .toBodilessEntity();
                delivered++;
            } catch (Exception exception) {
                failed++;
            }
        }

        return new EventDispatchResponse(delivered, failed);
    }
}
