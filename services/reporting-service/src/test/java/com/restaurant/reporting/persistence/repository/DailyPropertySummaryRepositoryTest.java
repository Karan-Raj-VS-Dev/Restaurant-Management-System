package com.restaurant.reporting.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.reporting.persistence.entity.DailyPropertySummaryEntity;
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
        "spring.datasource.url=jdbc:h2:mem:reportingrepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=reporting",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class DailyPropertySummaryRepositoryTest {

    @Autowired
    private DailyPropertySummaryRepository repository;

    @Test
    void findByTenantIdAndPropertyIdAndBusinessDateReturnsSavedSummary() {
        repository.save(summaryEntity("summary-001", "bikini-bottom", "krusty-krab", LocalDate.of(2026, 6, 15)));

        assertThat(repository.findByTenantIdAndPropertyIdAndBusinessDate(
                "bikini-bottom",
                "krusty-krab",
                LocalDate.of(2026, 6, 15)
        )).isPresent()
                .get()
                .satisfies(entity -> assertThat(ReflectionTestUtils.getField(entity, "summaryId")).isEqualTo("summary-001"));
    }

    private DailyPropertySummaryEntity summaryEntity(String summaryId, String tenantId, String propertyId, LocalDate businessDate) {
        DailyPropertySummaryEntity entity = instantiate(DailyPropertySummaryEntity.class);
        ReflectionTestUtils.setField(entity, "summaryId", summaryId);
        ReflectionTestUtils.setField(entity, "tenantId", tenantId);
        ReflectionTestUtils.setField(entity, "propertyId", propertyId);
        ReflectionTestUtils.setField(entity, "businessDate", businessDate);
        ReflectionTestUtils.setField(entity, "totalOrders", 12);
        ReflectionTestUtils.setField(entity, "dineInOrders", 8);
        ReflectionTestUtils.setField(entity, "takeawayOrders", 4);
        ReflectionTestUtils.setField(entity, "totalRevenue", new BigDecimal("3200.00"));
        ReflectionTestUtils.setField(entity, "totalTax", new BigDecimal("320.00"));
        ReflectionTestUtils.setField(entity, "totalDiscounts", new BigDecimal("45.00"));
        ReflectionTestUtils.setField(entity, "averageTicketSize", new BigDecimal("266.67"));
        ReflectionTestUtils.setField(entity, "generatedAt", Instant.parse("2026-06-15T10:00:00Z"));
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
