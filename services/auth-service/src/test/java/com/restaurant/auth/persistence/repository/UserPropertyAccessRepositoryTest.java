package com.restaurant.auth.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.auth.persistence.entity.UserPropertyAccessEntity;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:authmapping;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=auth",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UserPropertyAccessRepositoryTest {

    @Autowired
    private UserPropertyAccessRepository userPropertyAccessRepository;

    @Test
    void findByUserIdOrderByCreatedAtAscReturnsStablePropertyOrder() {
        userPropertyAccessRepository.save(mapping("upa-2", "usr-001", "patio", Instant.parse("2026-06-15T10:00:00Z")));
        userPropertyAccessRepository.save(mapping("upa-1", "usr-001", "krusty-krab", Instant.parse("2026-06-15T09:00:00Z")));

        List<UserPropertyAccessEntity> mappings = userPropertyAccessRepository.findByUserIdOrderByCreatedAtAsc("usr-001");

        assertThat(mappings).extracting(UserPropertyAccessEntity::getPropertyId).containsExactly("krusty-krab", "patio");
    }

    private UserPropertyAccessEntity mapping(String mappingId, String userId, String propertyId, Instant createdAt) {
        UserPropertyAccessEntity entity = new UserPropertyAccessEntity();
        entity.setMappingId(mappingId);
        entity.setUserId(userId);
        entity.setTenantId("bikini-bottom");
        entity.setPropertyId(propertyId);
        entity.setCreatedAt(createdAt);
        return entity;
    }
}
