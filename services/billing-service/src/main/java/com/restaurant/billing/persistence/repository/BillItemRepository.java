package com.restaurant.billing.persistence.repository;

import com.restaurant.billing.persistence.entity.BillItemEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillItemRepository extends JpaRepository<BillItemEntity, String> {

    List<BillItemEntity> findByBillIdOrderByBillItemIdAsc(String billId);

    List<BillItemEntity> findByBillIdIn(Collection<String> billIds);

    void deleteByBillId(String billId);
}
