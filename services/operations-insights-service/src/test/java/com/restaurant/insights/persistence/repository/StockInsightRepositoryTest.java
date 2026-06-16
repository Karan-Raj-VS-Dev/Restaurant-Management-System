package com.restaurant.insights.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.insights.persistence.entity.StockInsightEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:opsstockrepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=operations_insights",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class StockInsightRepositoryTest {

    @Autowired
    private StockInsightRepository repository;

    @Test
    void queriesReturnMatchingStockInsights() {
        repository.saveAll(List.of(
                entity("stock-001", "Cream", true, LocalDate.of(2026, 6, 15)),
                entity("stock-002", "Pasta", false, LocalDate.of(2026, 6, 15))
        ));

        assertThat(repository.findByTenantIdAndPropertyIdAndBusinessDate(
                "bikini-bottom",
                "krusty-krab",
                LocalDate.of(2026, 6, 15)
        )).hasSize(2);
        assertThat(repository.findByTenantIdAndPropertyIdAndLowStockTrue("bikini-bottom", "krusty-krab"))
                .singleElement()
                .satisfies(result -> assertThat(ReflectionTestUtils.getField(result, "ingredientName")).isEqualTo("Cream"));
    }

    private StockInsightEntity entity(String id, String ingredientName, boolean lowStock, LocalDate businessDate) {
        StockInsightEntity entity = instantiate(StockInsightEntity.class);
        ReflectionTestUtils.setField(entity, "stockInsightId", id);
        ReflectionTestUtils.setField(entity, "tenantId", "bikini-bottom");
        ReflectionTestUtils.setField(entity, "propertyId", "krusty-krab");
        ReflectionTestUtils.setField(entity, "businessDate", businessDate);
        ReflectionTestUtils.setField(entity, "ingredientId", "ing-" + id);
        ReflectionTestUtils.setField(entity, "ingredientName", ingredientName);
        ReflectionTestUtils.setField(entity, "currentQuantity", new BigDecimal("10.000"));
        ReflectionTestUtils.setField(entity, "reservedQuantity", new BigDecimal("1.000"));
        ReflectionTestUtils.setField(entity, "availableQuantity", new BigDecimal("9.000"));
        ReflectionTestUtils.setField(entity, "reorderLevel", new BigDecimal("2.000"));
        ReflectionTestUtils.setField(entity, "lowStock", lowStock);
        ReflectionTestUtils.setField(entity, "stockOut", false);
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
