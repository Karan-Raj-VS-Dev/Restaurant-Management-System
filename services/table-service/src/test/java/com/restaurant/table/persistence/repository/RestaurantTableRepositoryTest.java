package com.restaurant.table.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.table.persistence.entity.RestaurantTableEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:tablerepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=table_mgmt",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class RestaurantTableRepositoryTest {

    @Autowired
    private RestaurantTableRepository restaurantTableRepository;

    @Test
    void activeTableLookupAndScopedIdLookupWork() {
        restaurantTableRepository.saveAll(List.of(
                table("table-01", "T-01", true, "AVAILABLE"),
                table("table-02", "T-02", false, "UNAVAILABLE")
        ));

        assertThat(restaurantTableRepository.findByTenantIdAndPropertyIdAndActiveTrue("bikini-bottom", "krusty-krab"))
                .extracting(RestaurantTableEntity::getTableId)
                .containsExactly("table-01");
        assertThat(restaurantTableRepository.findByTenantIdAndPropertyIdAndTableId("bikini-bottom", "krusty-krab", "table-01"))
                .isPresent();
    }

    private RestaurantTableEntity table(String tableId, String tableNumber, boolean active, String status) {
        RestaurantTableEntity entity = new RestaurantTableEntity();
        entity.setTableId(tableId);
        entity.setTenantId("bikini-bottom");
        entity.setPropertyId("krusty-krab");
        entity.setTableNumber(tableNumber);
        entity.setDisplayName("Table " + tableNumber);
        entity.setFloorName("Main floor");
        entity.setSectionName("Dining");
        entity.setCapacity(4);
        entity.setStatus(status);
        entity.setActive(active);
        return entity;
    }
}
