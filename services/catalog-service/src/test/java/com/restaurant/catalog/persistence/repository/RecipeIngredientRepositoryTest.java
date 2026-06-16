package com.restaurant.catalog.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.catalog.persistence.entity.RecipeIngredientEntity;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:recipeingredientrepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=catalog",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class RecipeIngredientRepositoryTest {

    @Autowired
    private RecipeIngredientRepository repository;

    @Test
    void findByMenuItemIdsReturnsMatchingRecipeLines() {
        repository.saveAll(List.of(
                recipe("recipe-001", "item-001", "ing-001"),
                recipe("recipe-002", "item-002", "ing-002")
        ));

        assertThat(repository.findByMenuItemIdIn(List.of("item-001")))
                .extracting(RecipeIngredientEntity::getRecipeIngredientId)
                .containsExactly("recipe-001");
    }

    @Test
    void deleteByMenuItemIdRemovesMatchingRows() {
        repository.saveAll(List.of(
                recipe("recipe-001", "item-001", "ing-001"),
                recipe("recipe-002", "item-002", "ing-002")
        ));

        repository.deleteByMenuItemId("item-001");

        assertThat(repository.findAll())
                .extracting(RecipeIngredientEntity::getMenuItemId)
                .containsExactly("item-002");
    }

    private RecipeIngredientEntity recipe(String recipeIngredientId, String menuItemId, String ingredientId) {
        RecipeIngredientEntity entity = new RecipeIngredientEntity();
        entity.setRecipeIngredientId(recipeIngredientId);
        entity.setMenuItemId(menuItemId);
        entity.setIngredientId(ingredientId);
        entity.setQuantityRequired(BigDecimal.ONE);
        entity.setWastageFactor(BigDecimal.ZERO);
        return entity;
    }
}
