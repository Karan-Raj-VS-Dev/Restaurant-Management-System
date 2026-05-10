package com.restaurant.insights.persistence.repository;

import com.restaurant.insights.persistence.entity.DailyOperationsInsightEntity;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyOperationsInsightRepository extends JpaRepository<DailyOperationsInsightEntity, String> {

    Optional<DailyOperationsInsightEntity> findByTenantIdAndPropertyIdAndBusinessDate(String tenantId, String propertyId, LocalDate businessDate);
}
