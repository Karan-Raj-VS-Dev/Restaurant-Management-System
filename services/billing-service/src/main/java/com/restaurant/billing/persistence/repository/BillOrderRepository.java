package com.restaurant.billing.persistence.repository;

import com.restaurant.billing.persistence.entity.BillOrderEntity;
import com.restaurant.billing.persistence.entity.BillOrderId;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillOrderRepository extends JpaRepository<BillOrderEntity, BillOrderId> {

    List<BillOrderEntity> findByBillIdOrderByAttachedAtAsc(String billId);

    List<BillOrderEntity> findByBillIdIn(Collection<String> billIds);

    void deleteByBillId(String billId);
}
