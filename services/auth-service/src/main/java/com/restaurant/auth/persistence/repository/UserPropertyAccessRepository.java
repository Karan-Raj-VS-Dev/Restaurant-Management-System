package com.restaurant.auth.persistence.repository;

import com.restaurant.auth.persistence.entity.UserPropertyAccessEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserPropertyAccessRepository extends JpaRepository<UserPropertyAccessEntity, String> {

    List<UserPropertyAccessEntity> findByUserIdOrderByCreatedAtAsc(String userId);

    @Modifying
    @Transactional
    void deleteByUserId(String userId);
}
