package com.restaurant.inventory.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "catalog", name = "menu_items")
public class CatalogMenuItemEntity {

    @Id
    @Column(name = "menu_item_id", nullable = false, length = 64)
    private String menuItemId;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "property_id", nullable = false, length = 64)
    private String propertyId;

    @Column(name = "item_name", nullable = false, length = 150)
    private String itemName;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    protected CatalogMenuItemEntity() {
    }

    public String getMenuItemId() {
        return menuItemId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public String getItemName() {
        return itemName;
    }

    public boolean isActive() {
        return active;
    }
}
