package com.restaurant.integration.persistence.repository;

import com.restaurant.integration.persistence.entity.MarketplaceConnectorEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketplaceConnectorRepository extends JpaRepository<MarketplaceConnectorEntity, String> {

    List<MarketplaceConnectorEntity> findByTenantIdAndPropertyIdAndConnectorStatus(String tenantId, String propertyId, String connectorStatus);

    List<MarketplaceConnectorEntity> findByMarketplaceName(String marketplaceName);
}
