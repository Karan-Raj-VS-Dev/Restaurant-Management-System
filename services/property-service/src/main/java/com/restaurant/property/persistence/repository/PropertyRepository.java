package com.restaurant.property.persistence.repository;

import com.restaurant.property.persistence.entity.PropertyEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<PropertyEntity, String> {

    Optional<PropertyEntity> findByPropertyCode(String propertyCode);

    List<PropertyEntity> findByStatus(String status);

    Optional<PropertyEntity> findByTenantIdAndPropertyId(String tenantId, String propertyId);

    List<PropertyEntity> findByTenantIdAndStatusOrderByPropertyNameAsc(String tenantId, String status);

    List<PropertyEntity> findByTenantIdOrderByPropertyNameAsc(String tenantId);

    boolean existsByTenantIdAndPropertyNameIgnoreCase(String tenantId, String propertyName);
}
