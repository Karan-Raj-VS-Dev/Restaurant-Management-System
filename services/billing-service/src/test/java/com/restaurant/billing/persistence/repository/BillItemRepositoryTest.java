package com.restaurant.billing.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.billing.persistence.entity.BillItemEntity;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:billitemrepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=billing",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class BillItemRepositoryTest {

    @Autowired
    private BillItemRepository repository;

    @Test
    void findByBillIdOrdersByBillItemId() {
        repository.saveAll(List.of(
                item("bill-item-002", "bill-001", "Pasta Alfredo"),
                item("bill-item-001", "bill-001", "Margherita Pizza")
        ));

        assertThat(repository.findByBillIdOrderByBillItemIdAsc("bill-001"))
                .extracting(BillItemEntity::getBillItemId)
                .containsExactly("bill-item-001", "bill-item-002");
    }

    @Test
    void deleteByBillIdRemovesMatchingItems() {
        repository.saveAll(List.of(
                item("bill-item-001", "bill-001", "Margherita Pizza"),
                item("bill-item-002", "bill-002", "Pasta Alfredo")
        ));

        repository.deleteByBillId("bill-001");

        assertThat(repository.findAll())
                .extracting(BillItemEntity::getBillId)
                .containsExactly("bill-002");
    }

    private BillItemEntity item(String billItemId, String billId, String itemName) {
        BillItemEntity entity = new BillItemEntity();
        entity.setBillItemId(billItemId);
        entity.setBillId(billId);
        entity.setMenuItemId("menu-001");
        entity.setItemName(itemName);
        entity.setQuantity(1);
        entity.setUnitPrice(BigDecimal.valueOf(249));
        entity.setTaxAmount(BigDecimal.valueOf(12.45));
        entity.setLineTotal(BigDecimal.valueOf(261.45));
        return entity;
    }
}
