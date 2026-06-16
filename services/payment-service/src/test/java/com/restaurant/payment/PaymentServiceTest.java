package com.restaurant.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.restaurant.payment.persistence.entity.PaymentEntity;
import com.restaurant.payment.persistence.repository.PaymentRepository;
import com.restaurant.platform.eventing.DomainEventPublisher;
import com.restaurant.platform.eventing.EventEnvelope;
import com.restaurant.platform.eventing.EventEnvelopeFactory;
import com.restaurant.platform.eventing.contract.PaymentSucceededEvent;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                paymentRepository,
                new EventEnvelopeFactory("test-suite"),
                domainEventPublisher
        );
    }

    @Test
    void processPaymentPersistsSuccessfulPaymentAndPublishesEvent() {
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.processPayment(
                "bikini-bottom",
                "krusty-krab",
                new ProcessPaymentRequest("bill-001", PaymentMethod.CASH, new BigDecimal("1150.80"))
        );

        ArgumentCaptor<PaymentEntity> captor = ArgumentCaptor.forClass(PaymentEntity.class);
        verify(paymentRepository).save(captor.capture());
        PaymentEntity saved = captor.getValue();

        assertThat(saved.getPaymentId()).startsWith("pay-");
        assertThat(saved.getPaymentReference()).startsWith("ref-");
        assertThat(saved.getPaymentMethod()).isEqualTo(PaymentMethod.CASH.name());
        assertThat(saved.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS.name());
        assertThat(saved.getCurrencyCode()).isEqualTo("INR");
        assertThat(saved.getAmount()).isEqualByComparingTo("1150.80");

        assertThat(response.billId()).isEqualTo("bill-001");
        assertThat(response.method()).isEqualTo(PaymentMethod.CASH);
        assertThat(response.status()).isEqualTo(PaymentStatus.SUCCESS);

        ArgumentCaptor<EventEnvelope<?>> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(domainEventPublisher).publish(envelopeCaptor.capture());
        assertThat(envelopeCaptor.getValue().payload()).isInstanceOf(PaymentSucceededEvent.class);
        PaymentSucceededEvent payload = (PaymentSucceededEvent) envelopeCaptor.getValue().payload();
        assertThat(payload.billId()).isEqualTo("bill-001");
        assertThat(payload.tenantId()).isEqualTo("bikini-bottom");
        assertThat(payload.propertyId()).isEqualTo("krusty-krab");
        assertThat(payload.amount()).isEqualByComparingTo("1150.80");
        assertThat(payload.method()).isEqualTo(PaymentMethod.CASH.name());
        assertThat(payload.paidAt()).isEqualTo(response.paidAt());
    }
}
