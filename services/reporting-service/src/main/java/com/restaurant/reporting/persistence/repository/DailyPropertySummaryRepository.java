package com.restaurant.reporting.persistence.repository;

import com.restaurant.reporting.persistence.entity.DailyPropertySummaryEntity;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyPropertySummaryRepository extends JpaRepository<DailyPropertySummaryEntity, String> {

    Optional<DailyPropertySummaryEntity> findByTenantIdAndPropertyIdAndBusinessDate(String tenantId, String propertyId, LocalDate businessDate);
}
