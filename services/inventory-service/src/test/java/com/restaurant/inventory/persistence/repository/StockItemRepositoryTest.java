package com.restaurant.inventory.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.inventory.persistence.entity.StockItemEntity;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:inventoryrepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=inventory",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class StockItemRepositoryTest {

    @Autowired
    private StockItemRepository stockItemRepository;

    @Test
    void findByTenantPropertyAndIngredientIdReturnsMatchingStockItem() {
        stockItemRepository.save(stock("stock-001", "ing-001", "Cream"));

        assertThat(stockItemRepository.findByTenantIdAndPropertyIdAndIngredientId("bikini-bottom", "krusty-krab", "ing-001"))
                .get()
                .extracting(StockItemEntity::getStockItemId, StockItemEntity::getIngredientName)
                .containsExactly("stock-001", "Cream");
    }

    @Test
    void orderedLookupSortsByIngredientName() {
        stockItemRepository.saveAll(List.of(
                stock("stock-002", "ing-002", "Tomato Sauce"),
                stock("stock-001", "ing-001", "Cream")
        ));

        List<StockItemEntity> items = stockItemRepository.findByTenantIdAndPropertyIdOrderByIngredientNameAsc("bikini-bottom", "krusty-krab");

        assertThat(items).extracting(StockItemEntity::getIngredientName).containsExactly("Cream", "Tomato Sauce");
    }

    private StockItemEntity stock(String stockItemId, String ingredientId, String ingredientName) {
        StockItemEntity entity = new StockItemEntity();
        entity.setStockItemId(stockItemId);
        entity.setTenantId("bikini-bottom");
        entity.setPropertyId("krusty-krab");
        entity.setIngredientId(ingredientId);
        entity.setIngredientName(ingredientName);
        entity.setUnitOfMeasure("ml");
        entity.setReorderThreshold(BigDecimal.valueOf(2));
        entity.setMaximumCapacity(BigDecimal.valueOf(20));
        entity.setCurrentQuantity(BigDecimal.valueOf(10));
        entity.setReservedQuantity(BigDecimal.ZERO);
        entity.setAvailableQuantity(BigDecimal.valueOf(10));
        entity.setMarketUnitPrice(BigDecimal.valueOf(0.5));
        return entity;
    }
}
