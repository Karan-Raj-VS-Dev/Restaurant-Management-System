package com.restaurant.audit.persistence.repository;

import com.restaurant.audit.persistence.entity.AuditEventEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, String> {

    List<AuditEventEntity> findByAggregateTypeAndAggregateIdOrderByEventTimestampDesc(String aggregateType, String aggregateId);

    List<AuditEventEntity> findByTenantIdAndPropertyIdOrderByEventTimestampDesc(String tenantId, String propertyId);
}
