package com.restaurant.catalog.persistence.repository;

import com.restaurant.catalog.persistence.entity.MenuCategoryEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuCategoryRepository extends JpaRepository<MenuCategoryEntity, String> {

    Optional<MenuCategoryEntity> findByTenantIdAndPropertyIdAndCategoryNameIgnoreCase(String tenantId, String propertyId, String categoryName);

    List<MenuCategoryEntity> findByTenantIdAndPropertyIdAndActiveTrueOrderByDisplayOrderAscCategoryNameAsc(String tenantId, String propertyId);
}
