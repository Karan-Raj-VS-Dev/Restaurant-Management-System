package com.restaurant.eventgateway.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.eventgateway.persistence.entity.EventDispatchEntity;
import java.lang.reflect.Constructor;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:event-gateway;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=event_gateway",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
class EventDispatchRepositoryTest {

    @Autowired
    private EventDispatchRepository repository;

    @Test
    void findByDispatchStatusReturnsMatchingRows() throws Exception {
        repository.save(newDispatch("dispatch-001", "event-001", "SENT", Instant.parse("2026-06-15T06:00:00Z")));
        repository.save(newDispatch("dispatch-002", "event-002", "FAILED", Instant.parse("2026-06-15T06:01:00Z")));

        assertThat(repository.findByDispatchStatus("FAILED"))
                .extracting(entity -> ReflectionTestUtils.getField(entity, "dispatchId"))
                .containsExactly("dispatch-002");
    }

    @Test
    void findByEventIdReturnsAllDispatchesForEvent() throws Exception {
        repository.save(newDispatch("dispatch-101", "event-101", "SENT", Instant.parse("2026-06-15T06:00:00Z")));
        repository.save(newDispatch("dispatch-102", "event-101", "FAILED", Instant.parse("2026-06-15T06:01:00Z")));
        repository.save(newDispatch("dispatch-103", "event-999", "SENT", Instant.parse("2026-06-15T06:02:00Z")));

        assertThat(repository.findByEventId("event-101"))
                .extracting(entity -> ReflectionTestUtils.getField(entity, "dispatchId"))
                .containsExactlyInAnyOrder("dispatch-101", "dispatch-102");
    }

    private EventDispatchEntity newDispatch(String dispatchId, String eventId, String status, Instant dispatchedAt) throws Exception {
        EventDispatchEntity entity = newEntity(EventDispatchEntity.class);
        ReflectionTestUtils.setField(entity, "dispatchId", dispatchId);
        ReflectionTestUtils.setField(entity, "eventId", eventId);
        ReflectionTestUtils.setField(entity, "eventKey", "order.created");
        ReflectionTestUtils.setField(entity, "subscriberName", "kitchen-service");
        ReflectionTestUtils.setField(entity, "endpointUrl", "http://localhost/internal");
        ReflectionTestUtils.setField(entity, "dispatchStatus", status);
        ReflectionTestUtils.setField(entity, "responseStatusCode", "SENT".equals(status) ? 202 : 500);
        ReflectionTestUtils.setField(entity, "attemptCount", 1);
        ReflectionTestUtils.setField(entity, "errorMessage", "FAILED".equals(status) ? "boom" : null);
        ReflectionTestUtils.setField(entity, "dispatchedAt", dispatchedAt);
        ReflectionTestUtils.setField(entity, "completedAt", dispatchedAt.plusSeconds(5));
        return entity;
    }

    private <T> T newEntity(Class<T> type) throws Exception {
        Constructor<T> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }
}
