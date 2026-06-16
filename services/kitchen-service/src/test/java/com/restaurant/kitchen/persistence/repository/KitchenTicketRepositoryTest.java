package com.restaurant.kitchen.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.kitchen.persistence.entity.KitchenTicketEntity;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:kitchenrepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=kitchen",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class KitchenTicketRepositoryTest {

    @Autowired
    private KitchenTicketRepository kitchenTicketRepository;

    @Test
    void findByOrderIdReturnsMatchingTicket() {
        kitchenTicketRepository.save(ticket("ticket-001", "order-001", Instant.parse("2026-06-15T10:00:00Z"), KitchenTicketRepositoryTestStatuses.RECEIVED));

        assertThat(kitchenTicketRepository.findByOrderId("order-001"))
                .get()
                .extracting(KitchenTicketEntity::getTicketId)
                .isEqualTo("ticket-001");
    }

    @Test
    void listByTenantPropertyOrdersOldestFirst() {
        kitchenTicketRepository.saveAll(List.of(
                ticket("ticket-002", "order-002", Instant.parse("2026-06-15T11:00:00Z"), KitchenTicketRepositoryTestStatuses.RECEIVED),
                ticket("ticket-001", "order-001", Instant.parse("2026-06-15T10:00:00Z"), KitchenTicketRepositoryTestStatuses.ACCEPTED)
        ));

        List<KitchenTicketEntity> tickets = kitchenTicketRepository.findByTenantIdAndPropertyIdOrderByCreatedAtAsc("bikini-bottom", "krusty-krab");

        assertThat(tickets).extracting(KitchenTicketEntity::getTicketId).containsExactly("ticket-001", "ticket-002");
    }

    private KitchenTicketEntity ticket(String ticketId, String orderId, Instant createdAt, String status) {
        return KitchenTicketEntity.create(ticketId, orderId, "bikini-bottom", "krusty-krab", "table-01", status, "cook-001", createdAt);
    }

    private static class KitchenTicketRepositoryTestStatuses {
        private static final String RECEIVED = "RECEIVED";
        private static final String ACCEPTED = "ACCEPTED";
    }
}
