package com.restaurant.integration.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "marketplace_connectors")
public class MarketplaceConnectorEntity {

    @Id
    @Column(name = "connector_id", nullable = false, length = 64)
    private String connectorId;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "property_id", nullable = false, length = 64)
    private String propertyId;

    @Column(name = "marketplace_name", nullable = false, length = 64)
    private String marketplaceName;

    @Column(name = "connector_status", nullable = false, length = 32)
    private String connectorStatus;

    @Column(name = "external_store_id", length = 100)
    private String externalStoreId;

    @Column(name = "credentials_ref", length = 150)
    private String credentialsRef;

    @Column(name = "menu_sync_enabled", nullable = false)
    private boolean menuSyncEnabled;

    @Column(name = "order_ingestion_enabled", nullable = false)
    private boolean orderIngestionEnabled;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected MarketplaceConnectorEntity() {
    }
}
