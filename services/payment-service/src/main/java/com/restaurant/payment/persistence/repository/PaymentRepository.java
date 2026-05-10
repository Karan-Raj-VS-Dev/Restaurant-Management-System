package com.restaurant.payment.persistence.repository;

import com.restaurant.payment.persistence.entity.PaymentEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, String> {

    List<PaymentEntity> findByBillId(String billId);

    Optional<PaymentEntity> findByPaymentReference(String paymentReference);
}
