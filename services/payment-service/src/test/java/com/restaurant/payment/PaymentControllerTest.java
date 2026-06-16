package com.restaurant.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    private PaymentController controller;

    @BeforeEach
    void setUp() {
        controller = new PaymentController(paymentService);
    }

    @Test
    void processPaymentUsesDefaultScopeWhenNoPathOrQueryScopePresent() {
        ProcessPaymentRequest request = new ProcessPaymentRequest("bill-001", PaymentMethod.CASH, BigDecimal.valueOf(125));
        PaymentResponse expected = new PaymentResponse(
                "payment-001",
                "bill-001",
                PaymentMethod.CASH,
                PaymentStatus.SUCCESS,
                BigDecimal.valueOf(125),
                Instant.parse("2026-06-15T10:30:00Z")
        );
        when(paymentService.processPayment("bikini-bottom", "krusty-krab", request)).thenReturn(expected);

        PaymentResponse response = controller.processPayment(null, null, null, null, request);

        assertThat(response).isEqualTo(expected);
        verify(paymentService).processPayment("bikini-bottom", "krusty-krab", request);
    }

    @Test
    void processPaymentPrefersPathScope() {
        ProcessPaymentRequest request = new ProcessPaymentRequest("bill-002", PaymentMethod.UPI, BigDecimal.valueOf(499));
        PaymentResponse expected = new PaymentResponse(
                "payment-002",
                "bill-002",
                PaymentMethod.UPI,
                PaymentStatus.SUCCESS,
                BigDecimal.valueOf(499),
                Instant.parse("2026-06-15T10:45:00Z")
        );
        when(paymentService.processPayment("tenant-path", "property-path", request)).thenReturn(expected);

        PaymentResponse response = controller.processPayment("tenant-path", "property-path", "tenant-query", "property-query", request);

        assertThat(response).isEqualTo(expected);
        verify(paymentService).processPayment("tenant-path", "property-path", request);
    }
}
