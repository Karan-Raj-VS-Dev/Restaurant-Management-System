package com.restaurant.table.persistence.repository;

import com.restaurant.table.persistence.entity.TableSessionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TableSessionRepository extends JpaRepository<TableSessionEntity, String> {

    Optional<TableSessionEntity> findFirstByTenantIdAndPropertyIdAndTableIdAndSessionStatusOrderByStartedAtDesc(
            String tenantId,
            String propertyId,
            String tableId,
            String sessionStatus
    );

    List<TableSessionEntity> findByTenantIdAndPropertyIdAndSessionStatusOrderByStartedAtDesc(
            String tenantId,
            String propertyId,
            String sessionStatus
    );
}
