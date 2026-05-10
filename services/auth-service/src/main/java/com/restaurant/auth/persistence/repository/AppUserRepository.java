package com.restaurant.auth.persistence.repository;

import com.restaurant.auth.persistence.entity.AppUserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUserEntity, String> {

    Optional<AppUserEntity> findByUsernameIgnoreCase(String username);

    Optional<AppUserEntity> findByEmailIgnoreCase(String email);

    Optional<AppUserEntity> findByPhoneE164(String phoneE164);

    List<AppUserEntity> findAllByTenantIdOrderByCreatedAtDesc(String tenantId);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhoneE164(String phoneE164);
}
