package com.restaurant.insights.persistence.repository;

import com.restaurant.insights.persistence.entity.StockInsightEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockInsightRepository extends JpaRepository<StockInsightEntity, String> {

    List<StockInsightEntity> findByTenantIdAndPropertyIdAndBusinessDate(String tenantId, String propertyId, LocalDate businessDate);

    List<StockInsightEntity> findByTenantIdAndPropertyIdAndLowStockTrue(String tenantId, String propertyId);
}
