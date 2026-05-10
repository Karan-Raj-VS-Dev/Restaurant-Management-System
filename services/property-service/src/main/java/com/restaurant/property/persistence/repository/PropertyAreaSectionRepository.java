package com.restaurant.property.persistence.repository;

import com.restaurant.property.persistence.entity.PropertyAreaSectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PropertyAreaSectionRepository extends JpaRepository<PropertyAreaSectionEntity, String> {

    List<PropertyAreaSectionEntity> findByTenantIdAndPropertyIdOrderByFloorNameAscSectionNameAsc(String tenantId, String propertyId);

    Optional<PropertyAreaSectionEntity> findByTenantIdAndPropertyIdAndAreaSectionId(String tenantId, String propertyId, String areaSectionId);

    boolean existsByTenantIdAndPropertyIdAndAreaSectionId(String tenantId, String propertyId, String areaSectionId);
}
