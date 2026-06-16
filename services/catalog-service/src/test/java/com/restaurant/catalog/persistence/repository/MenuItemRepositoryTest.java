package com.restaurant.catalog.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.catalog.persistence.entity.MenuCategoryEntity;
import com.restaurant.catalog.persistence.entity.MenuItemEntity;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:catalogrepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=catalog",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class MenuItemRepositoryTest {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private MenuCategoryRepository menuCategoryRepository;

    @Test
    void activeItemsAreReturnedAlphabetically() {
        menuCategoryRepository.save(MenuCategoryEntity.create("cat-001", "bikini-bottom", "krusty-krab", "Italian dishes", 1, true));
        menuItemRepository.saveAll(List.of(
                menuItem("item-001", "dish-001", "Pasta Alfredo", true),
                menuItem("item-002", "dish-002", "Margherita Pizza", false),
                menuItem("item-003", "dish-003", "Arrabiata", true)
        ));

        List<MenuItemEntity> items = menuItemRepository.findByTenantIdAndPropertyIdAndActiveTrueOrderByItemNameAsc(
                "bikini-bottom",
                "krusty-krab"
        );

        assertThat(items).extracting(MenuItemEntity::getItemName)
                .containsExactly("Arrabiata", "Pasta Alfredo");
    }

    @Test
    void findByTenantIdAndPropertyIdAndItemCodeReturnsMatchingItem() {
        menuItemRepository.save(menuItem("item-001", "dish-001", "Pasta Alfredo", true));

        assertThat(menuItemRepository.findByTenantIdAndPropertyIdAndItemCode(
                "bikini-bottom",
                "krusty-krab",
                "dish-001"
        )).isPresent();
    }

    private MenuItemEntity menuItem(String menuItemId, String itemCode, String name, boolean active) {
        MenuItemEntity entity = new MenuItemEntity();
        entity.setMenuItemId(menuItemId);
        entity.setTenantId("bikini-bottom");
        entity.setPropertyId("krusty-krab");
        entity.setCategoryId("cat-001");
        entity.setItemCode(itemCode);
        entity.setItemName(name);
        entity.setDescription("Tasty");
        entity.setPrice(new BigDecimal("249.00"));
        entity.setVegetarian(false);
        entity.setActive(active);
        entity.setPrepTimeMinutes(10);
        return entity;
    }
}
