package com.restaurant.audit.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.audit.persistence.entity.AuditEventEntity;
import java.lang.reflect.Constructor;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:audit;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=audit",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
class AuditEventRepositoryTest {

    @Autowired
    private AuditEventRepository repository;

    @Test
    void findByTenantIdAndPropertyIdOrderByEventTimestampDescReturnsMatchingRows() throws Exception {
        AuditEventEntity older = newAuditEvent("audit-001", "event-001", "order", "order-001", "tenant-01", "property-01",
                Instant.parse("2026-06-15T05:00:00Z"));
        AuditEventEntity newer = newAuditEvent("audit-002", "event-002", "order", "order-001", "tenant-01", "property-01",
                Instant.parse("2026-06-15T06:00:00Z"));
        AuditEventEntity differentScope = newAuditEvent("audit-003", "event-003", "order", "order-002", "tenant-01", "property-02",
                Instant.parse("2026-06-15T07:00:00Z"));

        repository.save(older);
        repository.save(newer);
        repository.save(differentScope);

        assertThat(repository.findByTenantIdAndPropertyIdOrderByEventTimestampDesc("tenant-01", "property-01"))
                .extracting(entity -> ReflectionTestUtils.getField(entity, "auditEventId"))
                .containsExactly("audit-002", "audit-001");
    }

    @Test
    void findByAggregateTypeAndAggregateIdOrderByEventTimestampDescReturnsMatchingRows() throws Exception {
        AuditEventEntity first = newAuditEvent("audit-101", "event-101", "bill", "bill-001", "tenant-01", "property-01",
                Instant.parse("2026-06-15T05:00:00Z"));
        AuditEventEntity second = newAuditEvent("audit-102", "event-102", "bill", "bill-001", "tenant-01", "property-01",
                Instant.parse("2026-06-15T06:00:00Z"));
        AuditEventEntity third = newAuditEvent("audit-103", "event-103", "order", "order-001", "tenant-01", "property-01",
                Instant.parse("2026-06-15T07:00:00Z"));

        repository.save(first);
        repository.save(second);
        repository.save(third);

        assertThat(repository.findByAggregateTypeAndAggregateIdOrderByEventTimestampDesc("bill", "bill-001"))
                .extracting(entity -> ReflectionTestUtils.getField(entity, "auditEventId"))
                .containsExactly("audit-102", "audit-101");
    }

    private AuditEventEntity newAuditEvent(String auditEventId,
                                           String eventId,
                                           String aggregateType,
                                           String aggregateId,
                                           String tenantId,
                                           String propertyId,
                                           Instant occurredAt) throws Exception {
        AuditEventEntity entity = newEntity(AuditEventEntity.class);
        ReflectionTestUtils.setField(entity, "auditEventId", auditEventId);
        ReflectionTestUtils.setField(entity, "eventId", eventId);
        ReflectionTestUtils.setField(entity, "eventKey", aggregateType + ".updated");
        ReflectionTestUtils.setField(entity, "aggregateType", aggregateType);
        ReflectionTestUtils.setField(entity, "aggregateId", aggregateId);
        ReflectionTestUtils.setField(entity, "tenantId", tenantId);
        ReflectionTestUtils.setField(entity, "propertyId", propertyId);
        ReflectionTestUtils.setField(entity, "correlationId", eventId);
        ReflectionTestUtils.setField(entity, "causationId", null);
        ReflectionTestUtils.setField(entity, "producer", "audit-test");
        ReflectionTestUtils.setField(entity, "actorId", "emp-001");
        ReflectionTestUtils.setField(entity, "actorType", "EMPLOYEE");
        ReflectionTestUtils.setField(entity, "eventTimestamp", occurredAt);
        return entity;
    }

    private <T> T newEntity(Class<T> type) throws Exception {
        Constructor<T> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }
}
