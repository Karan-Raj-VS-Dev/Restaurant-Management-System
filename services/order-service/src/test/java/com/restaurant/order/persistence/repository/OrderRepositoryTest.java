package com.restaurant.order.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.order.persistence.entity.OrderEntity;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:orderrepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;NON_KEYWORDS=orders",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=ordering",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void findByTenantAndPropertyOrdersNewestFirst() {
        OrderEntity first = order("order-001", "CREATED", Instant.parse("2026-06-15T09:00:00Z"));
        OrderEntity latest = order("order-002", "IN_KITCHEN", Instant.parse("2026-06-15T10:00:00Z"));
        orderRepository.saveAll(List.of(first, latest));

        List<OrderEntity> orders = orderRepository.findByTenantIdAndPropertyIdOrderByOrderedAtDesc("bikini-bottom", "krusty-krab");

        assertThat(orders).extracting(OrderEntity::getOrderId).containsExactly("order-002", "order-001");
    }

    @Test
    void findByTenantPropertyAndOrderIdReturnsSingleMatch() {
        orderRepository.save(order("order-003", "SERVED", Instant.parse("2026-06-15T11:00:00Z")));

        assertThat(orderRepository.findByTenantIdAndPropertyIdAndOrderId("bikini-bottom", "krusty-krab", "order-003"))
                .get()
                .extracting(OrderEntity::getOrderStatus, OrderEntity::getSessionId)
                .containsExactly("SERVED", "session-01");
    }

    private OrderEntity order(String orderId, String status, Instant orderedAt) {
        OrderEntity entity = new OrderEntity();
        entity.setOrderId(orderId);
        entity.setTenantId("bikini-bottom");
        entity.setPropertyId("krusty-krab");
        entity.setTableId("table-01");
        entity.setSessionId("session-01");
        entity.setWaiterId("emp-01");
        entity.setOrderType("DINE_IN");
        entity.setOrderStatus(status);
        entity.setOrderedAt(orderedAt);
        entity.setUpdatedAt(orderedAt);
        return entity;
    }
}
