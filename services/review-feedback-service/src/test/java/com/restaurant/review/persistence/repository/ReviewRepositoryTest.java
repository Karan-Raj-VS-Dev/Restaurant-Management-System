package com.restaurant.review.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.review.persistence.entity.ReviewEntity;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:reviewrepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=review",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository repository;

    @Test
    void queriesReturnSavedReviews() {
        repository.save(entity("rev-001", "cust-001"));
        repository.save(entity("rev-002", "cust-002"));

        assertThat(repository.findByTenantIdAndPropertyId("bikini-bottom", "krusty-krab")).hasSize(2);
        assertThat(repository.findByCustomerId("cust-001"))
                .singleElement()
                .satisfies(review -> assertThat(ReflectionTestUtils.getField(review, "reviewId")).isEqualTo("rev-001"));
    }

    private ReviewEntity entity(String reviewId, String customerId) {
        ReviewEntity entity = instantiate(ReviewEntity.class);
        ReflectionTestUtils.setField(entity, "reviewId", reviewId);
        ReflectionTestUtils.setField(entity, "reviewRequestId", "rq-" + reviewId);
        ReflectionTestUtils.setField(entity, "tenantId", "bikini-bottom");
        ReflectionTestUtils.setField(entity, "propertyId", "krusty-krab");
        ReflectionTestUtils.setField(entity, "customerId", customerId);
        ReflectionTestUtils.setField(entity, "overallRating", (short) 5);
        ReflectionTestUtils.setField(entity, "foodRating", (short) 5);
        ReflectionTestUtils.setField(entity, "serviceRating", (short) 4);
        ReflectionTestUtils.setField(entity, "ambianceRating", (short) 4);
        ReflectionTestUtils.setField(entity, "comments", "Great");
        ReflectionTestUtils.setField(entity, "reviewStatus", "SUBMITTED");
        ReflectionTestUtils.setField(entity, "createdAt", Instant.parse("2026-06-15T10:00:00Z"));
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
