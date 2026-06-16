package com.restaurant.table.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.table.persistence.entity.TableSessionEntity;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:tablerepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=table_mgmt",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class TableSessionRepositoryTest {

    @Autowired
    private TableSessionRepository tableSessionRepository;

    @Test
    void findLatestOpenSessionForTableReturnsMostRecentOpenSession() {
        TableSessionEntity older = session("session-older", "table-01", "OPEN", Instant.parse("2026-06-15T09:00:00Z"));
        TableSessionEntity newer = session("session-newer", "table-01", "OPEN", Instant.parse("2026-06-15T10:00:00Z"));
        TableSessionEntity closed = session("session-closed", "table-01", "CLOSED", Instant.parse("2026-06-15T11:00:00Z"));
        tableSessionRepository.saveAll(List.of(older, newer, closed));

        assertThat(tableSessionRepository.findFirstByTenantIdAndPropertyIdAndTableIdAndSessionStatusOrderByStartedAtDesc(
                "bikini-bottom",
                "krusty-krab",
                "table-01",
                "OPEN"
        )).get().extracting(TableSessionEntity::getSessionId).isEqualTo("session-newer");
    }

    @Test
    void findAllOpenSessionsOrdersNewestFirst() {
        TableSessionEntity older = session("session-older", "table-01", "OPEN", Instant.parse("2026-06-15T09:00:00Z"));
        TableSessionEntity newer = session("session-newer", "table-02", "OPEN", Instant.parse("2026-06-15T10:00:00Z"));
        tableSessionRepository.saveAll(List.of(older, newer));

        List<TableSessionEntity> sessions = tableSessionRepository.findByTenantIdAndPropertyIdAndSessionStatusOrderByStartedAtDesc(
                "bikini-bottom",
                "krusty-krab",
                "OPEN"
        );

        assertThat(sessions).extracting(TableSessionEntity::getSessionId).containsExactly("session-newer", "session-older");
    }

    private TableSessionEntity session(String sessionId, String tableId, String status, Instant startedAt) {
        TableSessionEntity entity = new TableSessionEntity();
        entity.setSessionId(sessionId);
        entity.setTableId(tableId);
        entity.setTenantId("bikini-bottom");
        entity.setPropertyId("krusty-krab");
        entity.setOrderId("order-" + sessionId);
        entity.setCustomerId("cust-" + sessionId);
        entity.setCustomerCount(4);
        entity.setAssignedWaiterId("emp-01");
        entity.setStartedAt(startedAt);
        entity.setSessionStatus(status);
        return entity;
    }
}
