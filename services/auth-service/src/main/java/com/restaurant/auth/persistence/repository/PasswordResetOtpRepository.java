package com.restaurant.auth.persistence.repository;

import com.restaurant.auth.persistence.entity.PasswordResetOtpEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtpEntity, String> {

    Optional<PasswordResetOtpEntity> findTopByUserIdAndOtpCodeAndUsedAtIsNullOrderByCreatedAtDesc(String userId, String otpCode);

    void deleteByUserId(String userId);
}
