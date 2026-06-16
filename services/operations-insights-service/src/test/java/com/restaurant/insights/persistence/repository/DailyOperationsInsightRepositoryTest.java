package com.restaurant.insights.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.insights.persistence.entity.DailyOperationsInsightEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:opsdailyrepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=operations_insights",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class DailyOperationsInsightRepositoryTest {

    @Autowired
    private DailyOperationsInsightRepository repository;

    @Test
    void findByTenantIdAndPropertyIdAndBusinessDateReturnsSavedInsight() {
        repository.save(entity("insight-001", "bikini-bottom", "krusty-krab", LocalDate.of(2026, 6, 15)));

        assertThat(repository.findByTenantIdAndPropertyIdAndBusinessDate(
                "bikini-bottom",
                "krusty-krab",
                LocalDate.of(2026, 6, 15)
        )).isPresent()
                .get()
                .satisfies(result -> assertThat(ReflectionTestUtils.getField(result, "insightId")).isEqualTo("insight-001"));
    }

    private DailyOperationsInsightEntity entity(String id, String tenantId, String propertyId, LocalDate businessDate) {
        DailyOperationsInsightEntity entity = instantiate(DailyOperationsInsightEntity.class);
        ReflectionTestUtils.setField(entity, "insightId", id);
        ReflectionTestUtils.setField(entity, "tenantId", tenantId);
        ReflectionTestUtils.setField(entity, "propertyId", propertyId);
        ReflectionTestUtils.setField(entity, "businessDate", businessDate);
        ReflectionTestUtils.setField(entity, "totalOrders", 12);
        ReflectionTestUtils.setField(entity, "dineInOrders", 8);
        ReflectionTestUtils.setField(entity, "takeawayOrders", 4);
        ReflectionTestUtils.setField(entity, "completedPayments", 11);
        ReflectionTestUtils.setField(entity, "busiestTableId", "table-01");
        ReflectionTestUtils.setField(entity, "busiestTableCustomerCount", 5);
        ReflectionTestUtils.setField(entity, "topServerId", "emp-01");
        ReflectionTestUtils.setField(entity, "topServerCustomerCount", 5);
        ReflectionTestUtils.setField(entity, "kitchenActiveTickets", 2);
        ReflectionTestUtils.setField(entity, "totalRevenue", new BigDecimal("4000.00"));
        ReflectionTestUtils.setField(entity, "computedAt", Instant.parse("2026-06-15T10:00:00Z"));
        return entity;
    }

    private <T> T instantiate(Class<T> type) {
        try {
            var constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to create " + type.getSimpleName(), exception);
        }
    }
}
