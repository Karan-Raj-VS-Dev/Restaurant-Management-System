package com.restaurant.review.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.platform.eventing.DomainEventHandler;
import com.restaurant.platform.eventing.EventEnvelope;
import com.restaurant.platform.eventing.EventEnvelopeParser;
import com.restaurant.platform.eventing.EventKeys;
import com.restaurant.platform.eventing.contract.PaymentSucceededEvent;
import com.restaurant.review.ReviewService;
import org.springframework.stereotype.Component;

@Component
public class ReviewPaymentEventListener implements DomainEventHandler {

    private final ObjectMapper objectMapper;
    private final ReviewService reviewService;

    public ReviewPaymentEventListener(ObjectMapper objectMapper,
                                      ReviewService reviewService) {
        this.objectMapper = objectMapper;
        this.reviewService = reviewService;
    }

    @Override
    public void handle(String rawMessage) {
        EventEnvelope<com.fasterxml.jackson.databind.JsonNode> envelope = EventEnvelopeParser.parse(rawMessage, objectMapper);
        if (!EventKeys.PAYMENT_SUCCEEDED.equals(envelope.eventKey())) {
            return;
        }

        PaymentSucceededEvent event = objectMapper.convertValue(envelope.payload(), PaymentSucceededEvent.class);
        reviewService.registerReviewRequestFromPayment(event, envelope.eventId());
    }
}
