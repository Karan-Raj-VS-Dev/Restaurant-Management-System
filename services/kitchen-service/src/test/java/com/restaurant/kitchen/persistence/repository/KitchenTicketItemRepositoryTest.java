package com.restaurant.kitchen.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.kitchen.persistence.entity.KitchenTicketItemEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:kitchenticketitems;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=kitchen",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class KitchenTicketItemRepositoryTest {

    @Autowired
    private KitchenTicketItemRepository repository;

    @Test
    void findByTicketIdReturnsItemsOrderedByOrderItemId() {
        repository.saveAll(List.of(
                KitchenTicketItemEntity.create("kti-002", "ticket-001", "order-item-002", "menu-002", "Pasta Alfredo", 1, "RECEIVED"),
                KitchenTicketItemEntity.create("kti-001", "ticket-001", "order-item-001", "menu-001", "Margherita Pizza", 2, "RECEIVED")
        ));

        assertThat(repository.findByTicketIdOrderByOrderItemIdAsc("ticket-001"))
                .extracting(KitchenTicketItemEntity::getOrderItemId)
                .containsExactly("order-item-001", "order-item-002");
    }

    @Test
    void deleteByTicketIdRemovesOnlyMatchingItems() {
        repository.saveAll(List.of(
                KitchenTicketItemEntity.create("kti-001", "ticket-001", "order-item-001", "menu-001", "Margherita Pizza", 2, "RECEIVED"),
                KitchenTicketItemEntity.create("kti-002", "ticket-002", "order-item-002", "menu-002", "Pasta Alfredo", 1, "RECEIVED")
        ));

        repository.deleteByTicketId("ticket-001");

        assertThat(repository.findAll())
                .extracting(KitchenTicketItemEntity::getTicketId)
                .containsExactly("ticket-002");
    }
}
