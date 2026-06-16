package com.restaurant.integration.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.integration.persistence.entity.MarketplaceConnectorEntity;
import java.lang.reflect.Constructor;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:marketplace;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=marketplace_integration",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
class MarketplaceConnectorRepositoryTest {

    @Autowired
    private MarketplaceConnectorRepository repository;

    @Test
    void findByTenantIdAndPropertyIdAndConnectorStatusReturnsMatchingRows() throws Exception {
        repository.save(newConnector("conn-001", "bikini-bottom", "krusty-krab", "Swiggy", "ACTIVE"));
        repository.save(newConnector("conn-002", "bikini-bottom", "krusty-krab", "Zomato", "INACTIVE"));

        assertThat(repository.findByTenantIdAndPropertyIdAndConnectorStatus("bikini-bottom", "krusty-krab", "ACTIVE"))
                .extracting(entity -> ReflectionTestUtils.getField(entity, "connectorId"))
                .containsExactly("conn-001");
    }

    @Test
    void findByMarketplaceNameReturnsMatchingRows() throws Exception {
        repository.save(newConnector("conn-101", "bikini-bottom", "krusty-krab", "Swiggy", "ACTIVE"));
        repository.save(newConnector("conn-102", "bikini-bottom", "krusty-krab", "Swiggy", "INACTIVE"));
        repository.save(newConnector("conn-103", "bikini-bottom", "krusty-krab", "Zomato", "ACTIVE"));

        assertThat(repository.findByMarketplaceName("Swiggy"))
                .extracting(entity -> ReflectionTestUtils.getField(entity, "connectorId"))
                .containsExactlyInAnyOrder("conn-101", "conn-102");
    }

    private MarketplaceConnectorEntity newConnector(String connectorId,
                                                    String tenantId,
                                                    String propertyId,
                                                    String marketplaceName,
                                                    String connectorStatus) throws Exception {
        MarketplaceConnectorEntity entity = newEntity(MarketplaceConnectorEntity.class);
        Instant now = Instant.parse("2026-06-15T07:30:00Z");
        ReflectionTestUtils.setField(entity, "connectorId", connectorId);
        ReflectionTestUtils.setField(entity, "tenantId", tenantId);
        ReflectionTestUtils.setField(entity, "propertyId", propertyId);
        ReflectionTestUtils.setField(entity, "marketplaceName", marketplaceName);
        ReflectionTestUtils.setField(entity, "connectorStatus", connectorStatus);
        ReflectionTestUtils.setField(entity, "externalStoreId", marketplaceName.toLowerCase() + "-store");
        ReflectionTestUtils.setField(entity, "credentialsRef", "secret/" + connectorId);
        ReflectionTestUtils.setField(entity, "menuSyncEnabled", true);
        ReflectionTestUtils.setField(entity, "orderIngestionEnabled", true);
        ReflectionTestUtils.setField(entity, "createdAt", now);
        ReflectionTestUtils.setField(entity, "updatedAt", now.plusSeconds(60));
        return entity;
    }

    private <T> T newEntity(Class<T> type) throws Exception {
        Constructor<T> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }
}
