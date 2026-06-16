package com.restaurant.takeaway.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.takeaway.persistence.entity.TakeawayOrderEntity;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:takeawayrepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=takeaway",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class TakeawayOrderRepositoryTest {

    @Autowired
    private TakeawayOrderRepository repository;

    @Test
    void repositoryQueriesReturnMatchingOrders() {
        repository.save(order("to-001", "SWIGGY", "CREATED"));
        repository.save(order("to-002", "DIRECT", "READY"));

        assertThat(repository.findBySourceChannel("SWIGGY"))
                .singleElement()
                .satisfies(entity -> assertThat(ReflectionTestUtils.getField(entity, "takeawayOrderId")).isEqualTo("to-001"));
        assertThat(repository.findByTenantIdAndPropertyIdAndTakeawayStatus("bikini-bottom", "krusty-krab", "READY"))
                .singleElement()
                .satisfies(entity -> assertThat(ReflectionTestUtils.getField(entity, "takeawayOrderId")).isEqualTo("to-002"));
    }

    private TakeawayOrderEntity order(String orderId, String channel, String status) {
        TakeawayOrderEntity entity = instantiate(TakeawayOrderEntity.class);
        ReflectionTestUtils.setField(entity, "takeawayOrderId", orderId);
        ReflectionTestUtils.setField(entity, "tenantId", "bikini-bottom");
        ReflectionTestUtils.setField(entity, "propertyId", "krusty-krab");
        ReflectionTestUtils.setField(entity, "customerId", "cust-001");
        ReflectionTestUtils.setField(entity, "customerName", "Dom");
        ReflectionTestUtils.setField(entity, "customerPhone", "8901913123");
        ReflectionTestUtils.setField(entity, "fulfillmentType", "PICKUP");
        ReflectionTestUtils.setField(entity, "sourceChannel", channel);
        ReflectionTestUtils.setField(entity, "takeawayStatus", status);
        ReflectionTestUtils.setField(entity, "subtotalAmount", new BigDecimal("200.00"));
        ReflectionTestUtils.setField(entity, "taxAmount", new BigDecimal("20.00"));
        ReflectionTestUtils.setField(entity, "deliveryFee", BigDecimal.ZERO);
        ReflectionTestUtils.setField(entity, "totalAmount", new BigDecimal("220.00"));
        ReflectionTestUtils.setField(entity, "createdAt", Instant.parse("2026-06-15T10:00:00Z"));
        ReflectionTestUtils.setField(entity, "updatedAt", Instant.parse("2026-06-15T10:00:00Z"));
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
