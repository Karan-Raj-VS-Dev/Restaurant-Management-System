package com.restaurant.inventory.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.platform.eventing.DomainEventPublisher;
import com.restaurant.platform.eventing.EventEnvelopeFactory;
import com.restaurant.platform.eventing.HttpDomainEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class EventingConfiguration {

    @Bean
    EventEnvelopeFactory eventEnvelopeFactory(@Value("${spring.application.name}") String applicationName) {
        return new EventEnvelopeFactory(applicationName);
    }

    @Bean
    DomainEventPublisher domainEventPublisher(ObjectMapper objectMapper,
                                              @Value("${app.eventing.gateway-url:http://localhost:9018}") String gatewayUrl) {
        return new HttpDomainEventPublisher(RestClient.builder().build(), objectMapper, gatewayUrl);
    }
}
