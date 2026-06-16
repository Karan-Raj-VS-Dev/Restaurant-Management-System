package com.restaurant.inventory.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.inventory.persistence.entity.CatalogMenuItemEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:inventorycatalogmenuitemrepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=catalog",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class CatalogMenuItemRepositoryTest {

    @Autowired
    private CatalogMenuItemRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void activeMenuItemsAreReturnedAlphabetically() {
        insertMenuItem("item-002", "Pasta Alfredo", true);
        insertMenuItem("item-003", "Arrabiata", true);
        insertMenuItem("item-001", "Margherita Pizza", false);

        assertThat(repository.findByTenantIdAndPropertyIdAndActiveTrueOrderByItemNameAsc("bikini-bottom", "krusty-krab"))
                .extracting(CatalogMenuItemEntity::getItemName)
                .containsExactly("Arrabiata", "Pasta Alfredo");
    }

    private void insertMenuItem(String menuItemId, String itemName, boolean active) {
        jdbcTemplate.update(
                """
                insert into catalog.menu_items
                (menu_item_id, tenant_id, property_id, item_name, is_active)
                values (?, ?, ?, ?, ?)
                """,
                menuItemId,
                "bikini-bottom",
                "krusty-krab",
                itemName,
                active
        );
    }
}
