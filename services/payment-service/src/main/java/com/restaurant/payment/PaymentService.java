package com.restaurant.payment;

import com.restaurant.platform.eventing.AggregateTypes;
import com.restaurant.platform.eventing.DomainEventPublisher;
import com.restaurant.platform.eventing.EventEnvelopeFactory;
import com.restaurant.platform.eventing.EventKeys;
import com.restaurant.platform.eventing.contract.PaymentSucceededEvent;
import com.restaurant.payment.persistence.entity.PaymentEntity;
import com.restaurant.payment.persistence.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final EventEnvelopeFactory eventEnvelopeFactory;
    private final DomainEventPublisher domainEventPublisher;

    public PaymentService(PaymentRepository paymentRepository,
                          EventEnvelopeFactory eventEnvelopeFactory,
                          DomainEventPublisher domainEventPublisher) {
        this.paymentRepository = paymentRepository;
        this.eventEnvelopeFactory = eventEnvelopeFactory;
        this.domainEventPublisher = domainEventPublisher;
    }

    public PaymentResponse processPayment(String tenantId, String propertyId, ProcessPaymentRequest request) {
        Instant now = Instant.now();
        PaymentEntity entity = PaymentEntity.create(
                "pay-" + UUID.randomUUID(),
                request.billId(),
                null,
                tenantId,
                propertyId,
                "ref-" + UUID.randomUUID(),
                request.method().name(),
                PaymentStatus.SUCCESS.name(),
                request.amount(),
                "INR",
                now,
                now,
                now
        );
        PaymentEntity saved = paymentRepository.save(entity);

        PaymentResponse response = new PaymentResponse(
                saved.getPaymentId(),
                saved.getBillId(),
                PaymentMethod.valueOf(saved.getPaymentMethod()),
                PaymentStatus.valueOf(saved.getPaymentStatus()),
                saved.getAmount(),
                saved.getPaidAt()
        );

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
