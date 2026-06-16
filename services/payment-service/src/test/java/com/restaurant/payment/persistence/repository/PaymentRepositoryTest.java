package com.restaurant.payment.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.payment.persistence.entity.PaymentEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:paymentrepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=payment",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void findByBillIdAndPaymentReferenceReturnMatchingPayments() {
        paymentRepository.saveAll(List.of(
                payment("pay-001", "bill-001", "ref-001"),
                payment("pay-002", "bill-001", "ref-002"),
                payment("pay-003", "bill-002", "ref-003")
        ));

        assertThat(paymentRepository.findByBillId("bill-001"))
                .extracting(PaymentEntity::getPaymentId)
                .containsExactlyInAnyOrder("pay-001", "pay-002");
        assertThat(paymentRepository.findByPaymentReference("ref-003"))
                .get()
                .extracting(PaymentEntity::getBillId)
                .isEqualTo("bill-002");
    }

    private PaymentEntity payment(String paymentId, String billId, String reference) {
        Instant now = Instant.parse("2026-06-15T12:00:00Z");
        return PaymentEntity.create(
                paymentId,
                billId,
                "order-001",
                "bikini-bottom",
                "krusty-krab",
                reference,
                "CASH",
                "SUCCESS",
                new BigDecimal("249.00"),
                "INR",
                now,
                now,
                now
        );
    }
}
