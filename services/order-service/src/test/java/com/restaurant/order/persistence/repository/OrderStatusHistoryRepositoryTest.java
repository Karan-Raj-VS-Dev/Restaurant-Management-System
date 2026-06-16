package com.restaurant.order.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.order.OrderStatus;
import com.restaurant.order.OrderStatusTrailEntry;
import com.restaurant.order.persistence.entity.OrderStatusHistoryEntity;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:orderstatushistoryrepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=ordering",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class OrderStatusHistoryRepositoryTest {

    @Autowired
    private OrderStatusHistoryRepository repository;

    @Test
    void savePersistsCurrentStatusAndJsonTrailForSingleOrderRow() {
        OrderStatusHistoryEntity entity = new OrderStatusHistoryEntity();
        entity.setOrderId("order-001");
        entity.setStatus(OrderStatus.SERVED.name());
        entity.setStatusTrail(List.of(
                new OrderStatusTrailEntry(
                        OrderStatus.CREATED,
                        "emp-101",
                        "Order created.",
                        Instant.parse("2026-06-15T10:00:00Z")
                ),
                new OrderStatusTrailEntry(
                        OrderStatus.SERVED,
                        "emp-101",
                        "Order served to the customer.",
                        Instant.parse("2026-06-15T10:20:00Z")
                )
        ));

        repository.save(entity);

        OrderStatusHistoryEntity persisted = repository.findById("order-001").orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(OrderStatus.SERVED.name());
        assertThat(persisted.getUpdatedAt()).isNotNull();
        assertThat(persisted.getStatusTrail())
                .extracting(OrderStatusTrailEntry::status)
                .containsExactly(OrderStatus.CREATED, OrderStatus.SERVED);
        assertThat(persisted.getStatusTrail())
                .extracting(OrderStatusTrailEntry::remarks)
                .containsExactly("Order created.", "Order served to the customer.");
    }
}
