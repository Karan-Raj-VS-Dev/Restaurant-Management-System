package com.restaurant.inventory.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.inventory.persistence.entity.CatalogIngredientEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:inventorycatalogingredientrepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=catalog",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class CatalogIngredientRepositoryTest {

    @Autowired
    private CatalogIngredientRepository repository;

    @Test
    void findByTenantPropertyAndIngredientIdReturnsMatch() {
        repository.save(ingredient("ing-001", "Cream"));

        assertThat(repository.findByTenantIdAndPropertyIdAndIngredientId("bikini-bottom", "krusty-krab", "ing-001")).isPresent();
    }

    @Test
    void findByIngredientIdsReturnsRequestedSubset() {
        repository.saveAll(List.of(
                ingredient("ing-001", "Cream"),
                ingredient("ing-002", "Pasta"),
                ingredient("ing-003", "Mozzarella")
        ));

        assertThat(repository.findByTenantIdAndPropertyIdAndIngredientIdIn("bikini-bottom", "krusty-krab", List.of("ing-001", "ing-003")))
                .extracting(CatalogIngredientEntity::getIngredientId)
                .containsExactlyInAnyOrder("ing-001", "ing-003");
    }

    private CatalogIngredientEntity ingredient(String ingredientId, String name) {
        CatalogIngredientEntity entity = new CatalogIngredientEntity();
        entity.setIngredientId(ingredientId);
        entity.setTenantId("bikini-bottom");
        entity.setPropertyId("krusty-krab");
        entity.setIngredientCode(ingredientId.toUpperCase());
        entity.setIngredientName(name);
        entity.setUnitOfMeasure("grams");
        entity.setActive(true);
        return entity;
    }
}
