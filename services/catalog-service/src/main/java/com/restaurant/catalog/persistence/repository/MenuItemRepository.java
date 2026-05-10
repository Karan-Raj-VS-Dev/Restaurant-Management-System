package com.restaurant.catalog.persistence.repository;

import com.restaurant.catalog.persistence.entity.MenuItemEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItemEntity, String> {

    List<MenuItemEntity> findByTenantIdAndPropertyIdAndActiveTrueOrderByItemNameAsc(String tenantId, String propertyId);

    List<MenuItemEntity> findByTenantIdAndPropertyIdOrderByItemNameAsc(String tenantId, String propertyId);

    Optional<MenuItemEntity> findByTenantIdAndPropertyIdAndItemCode(String tenantId, String propertyId, String itemCode);

    Optional<MenuItemEntity> findByTenantIdAndPropertyIdAndMenuItemId(String tenantId, String propertyId, String menuItemId);
}
