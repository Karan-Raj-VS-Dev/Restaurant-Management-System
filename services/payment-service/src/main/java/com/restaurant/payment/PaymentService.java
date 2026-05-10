package com.restaurant.payment;

import com.restaurant.platform.eventing.AggregateTypes;
import com.restaurant.platform.eventing.DomainEventPublisher;
import com.restaurant.platform.eventing.EventEnvelopeFactory;
import com.restaurant.platform.eventing.EventKeys;
import com.restaurant.platform.eventing.contract.PaymentSucceededEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentService {

    private final Map<String, PaymentResponse> payments = new ConcurrentHashMap<>();
    private final EventEnvelopeFactory eventEnvelopeFactory;
    private final DomainEventPublisher domainEventPublisher;

    public PaymentService(EventEnvelopeFactory eventEnvelopeFactory,
                          DomainEventPublisher domainEventPublisher) {
        this.eventEnvelopeFactory = eventEnvelopeFactory;
        this.domainEventPublisher = domainEventPublisher;
    }

    public PaymentResponse processPayment(String tenantId, String propertyId, ProcessPaymentRequest request) {
        PaymentResponse response = new PaymentResponse(
                "pay-" + UUID.randomUUID(),
                request.billId(),
                request.method(),
                PaymentStatus.SUCCESS,
                request.amount(),
                Instant.now()
        );
        payments.put(response.paymentId(), response);

        domainEventPublisher.publish(eventEnvelopeFactory.create(
                EventKeys.PAYMENT_SUCCEEDED,
                AggregateTypes.PAYMENT,
                response.paymentId(),
                request.billId(),
                request.billId(),
                null,
                new PaymentSucceededEvent(
                        response.paymentId(),
                        response.billId(),
                        tenantId,
                        propertyId,
                        response.amount(),
                        response.method().name(),
                        response.paidAt()
                )
        ));
        return response;
    }
}
