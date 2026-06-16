package com.restaurant.auth.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.auth.persistence.entity.PasswordResetOtpEntity;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:authotp;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=auth",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class PasswordResetOtpRepositoryTest {

    @Autowired
    private PasswordResetOtpRepository passwordResetOtpRepository;

    @Test
    void findTopByUserIdAndOtpCodeAndUsedAtIsNullOrderByCreatedAtDescReturnsLatestUnusedOtp() {
        passwordResetOtpRepository.save(otp("otp-older", "usr-001", "123456", Instant.parse("2026-06-15T09:00:00Z"), null));
        passwordResetOtpRepository.save(otp("otp-newer", "usr-001", "123456", Instant.parse("2026-06-15T10:00:00Z"), null));

        assertThat(passwordResetOtpRepository.findTopByUserIdAndOtpCodeAndUsedAtIsNullOrderByCreatedAtDesc("usr-001", "123456"))
                .get()
                .extracting(PasswordResetOtpEntity::getOtpId)
                .isEqualTo("otp-newer");
    }

    @Test
    void deleteByUserIdRemovesAllOtpsForUser() {
        passwordResetOtpRepository.saveAll(List.of(
                otp("otp-1", "usr-001", "123456", Instant.parse("2026-06-15T09:00:00Z"), null),
                otp("otp-2", "usr-001", "654321", Instant.parse("2026-06-15T09:05:00Z"), null)
        ));

        passwordResetOtpRepository.deleteByUserId("usr-001");

        assertThat(passwordResetOtpRepository.findAll()).isEmpty();
    }

    private PasswordResetOtpEntity otp(String otpId, String userId, String code, Instant createdAt, Instant usedAt) {
        PasswordResetOtpEntity entity = new PasswordResetOtpEntity();
        entity.setOtpId(otpId);
        entity.setUserId(userId);
        entity.setIdentifier("karan@restaurant.local");
        entity.setDeliveryChannel("EMAIL");
        entity.setOtpCode(code);
        entity.setExpiresAt(createdAt.plusSeconds(600));
        entity.setUsedAt(usedAt);
        entity.setCreatedAt(createdAt);
        return entity;
    }
}
