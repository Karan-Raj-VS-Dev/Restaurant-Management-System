package com.restaurant.auth.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.auth.persistence.entity.AppUserEntity;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:authusers;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=auth",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class AppUserRepositoryTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    void findByUsernameIgnoreCaseMatchesStoredUser() {
        appUserRepository.save(user("usr-001", "KaranRaj", "karan@restaurant.local", "+918901913123", Instant.parse("2026-06-15T09:00:00Z")));

        assertThat(appUserRepository.findByUsernameIgnoreCase("karanraj"))
                .get()
                .extracting(AppUserEntity::getUserId, AppUserEntity::getEmail)
                .containsExactly("usr-001", "karan@restaurant.local");
    }

    @Test
    void findAllByTenantIdOrdersNewestFirst() {
        AppUserEntity older = user("usr-older", "older", "older@restaurant.local", "+919000000001", Instant.parse("2026-06-15T09:00:00Z"));
        AppUserEntity newer = user("usr-newer", "newer", "newer@restaurant.local", "+919000000002", Instant.parse("2026-06-15T10:00:00Z"));
        appUserRepository.saveAll(List.of(older, newer));

        List<AppUserEntity> users = appUserRepository.findAllByTenantIdOrderByCreatedAtDesc("bikini-bottom");

        assertThat(users).extracting(AppUserEntity::getUserId).containsExactly("usr-newer", "usr-older");
    }

    @Test
    void findByPhoneE164ReturnsUniquePhoneMatch() {
        appUserRepository.save(user("usr-002", "phoneUser", "phone@restaurant.local", "+919111111111", Instant.parse("2026-06-15T11:00:00Z")));

        assertThat(appUserRepository.findByPhoneE164("+919111111111"))
                .get()
                .extracting(AppUserEntity::getUsername)
                .isEqualTo("phoneUser");
    }

    private AppUserEntity user(String userId, String username, String email, String phoneE164, Instant createdAt) {
        AppUserEntity entity = new AppUserEntity();
        entity.setUserId(userId);
        entity.setTenantId("bikini-bottom");
        entity.setUsername(username);
        entity.setEmail(email);
        entity.setPassword("Password@123");
        entity.setFullName("Test User");
        entity.setFirstName("Test");
        entity.setLastName("User");
        entity.setPhoneCountryCode("+91");
        entity.setPhoneNumber(phoneE164.substring(3));
        entity.setPhoneE164(phoneE164);
        entity.setStatus("ACTIVE");
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(createdAt);
        return entity;
    }
}
