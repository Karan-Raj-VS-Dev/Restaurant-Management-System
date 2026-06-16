package com.restaurant.billing.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.billing.persistence.entity.BillEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:billingrepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=billing",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class BillRepositoryTest {

    @Autowired
    private BillRepository billRepository;

    @Test
    void findByLastOrderIdReturnsMatchingBill() {
        BillEntity bill = bill("bill-001", "order-001", "session-001", Instant.parse("2026-06-15T10:00:00Z"), "DRAFT");
        billRepository.save(bill);

        assertThat(billRepository.findByLastOrderId("order-001"))
                .get()
                .extracting(BillEntity::getBillId, BillEntity::getSessionId)
                .containsExactly("bill-001", "session-001");
    }

    @Test
    void findLatestActiveBillBySessionReturnsNewestMatchingStatus() {
        BillEntity older = bill("bill-older", "order-001", "session-001", Instant.parse("2026-06-15T09:00:00Z"), "DRAFT");
        BillEntity newer = bill("bill-newer", "order-002", "session-001", Instant.parse("2026-06-15T10:00:00Z"), "FINALIZED");
        BillEntity paid = bill("bill-paid", "order-003", "session-001", Instant.parse("2026-06-15T11:00:00Z"), "PAID");
        billRepository.saveAll(List.of(older, newer, paid));

        assertThat(billRepository.findFirstByTenantIdAndPropertyIdAndSessionIdAndBillingStatusInOrderByGeneratedAtDesc(
                "bikini-bottom",
                "krusty-krab",
                "session-001",
                List.of("DRAFT", "FINALIZED")
        )).get().extracting(BillEntity::getBillId).isEqualTo("bill-newer");
    }

    private BillEntity bill(String billId, String lastOrderId, String sessionId, Instant generatedAt, String status) {
        BillEntity entity = new BillEntity();
        entity.setBillId(billId);
        entity.setLastOrderId(lastOrderId);
        entity.setTenantId("bikini-bottom");
        entity.setPropertyId("krusty-krab");
        entity.setTableId("table-01");
        entity.setSessionId(sessionId);
        entity.setBillingStatus(status);
        entity.setSettlementType("STANDARD");
        entity.setCancellationFeeAmount(BigDecimal.ZERO);
        entity.setSubtotalAmount(BigDecimal.valueOf(299));
        entity.setTaxAmount(BigDecimal.valueOf(14.95));
        entity.setServiceChargeAmount(BigDecimal.ZERO);
        entity.setDiscountAmount(BigDecimal.ZERO);
        entity.setTotalAmount(BigDecimal.valueOf(313.95));
        entity.setGeneratedAt(generatedAt);
        return entity;
    }
}
