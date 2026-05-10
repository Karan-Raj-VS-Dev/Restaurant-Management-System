package com.restaurant.table.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "restaurant_tables")
public class RestaurantTableEntity {

    @Id
    @Column(name = "table_id", nullable = false, length = 64)
    private String tableId;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "property_id", nullable = false, length = 64)
    private String propertyId;

    @Column(name = "table_number", nullable = false, length = 32)
    private String tableNumber;

    @Column(name = "floor_name", length = 64)
    private String floorName;

    @Column(name = "section_name", length = 64)
    private String sectionName;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RestaurantTableEntity() {
    }
}
