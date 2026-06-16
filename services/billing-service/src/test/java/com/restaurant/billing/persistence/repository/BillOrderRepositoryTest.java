package com.restaurant.billing.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.billing.persistence.entity.BillOrderEntity;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:billorderrepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=billing",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class BillOrderRepositoryTest {

    @Autowired
    private BillOrderRepository repository;

    @Test
    void findByBillIdOrdersByAttachedAt() {
        repository.saveAll(List.of(
                order("bill-001", "order-002", Instant.parse("2026-06-15T11:00:00Z")),
                order("bill-001", "order-001", Instant.parse("2026-06-15T10:00:00Z"))
        ));

        assertThat(repository.findByBillIdOrderByAttachedAtAsc("bill-001"))
                .extracting(BillOrderEntity::getOrderId)
                .containsExactly("order-001", "order-002");
    }

    @Test
    void deleteByBillIdRemovesMatchingRows() {
        repository.saveAll(List.of(
                order("bill-001", "order-001", Instant.parse("2026-06-15T10:00:00Z")),
                order("bill-002", "order-002", Instant.parse("2026-06-15T10:10:00Z"))
        ));

        repository.deleteByBillId("bill-001");

        assertThat(repository.findAll())
                .extracting(BillOrderEntity::getBillId)
                .containsExactly("bill-002");
    }

    private BillOrderEntity order(String billId, String orderId, Instant attachedAt) {
        BillOrderEntity entity = new BillOrderEntity();
        entity.setBillId(billId);
        entity.setOrderId(orderId);
        entity.setAttachedAt(attachedAt);
        return entity;
    }
}
