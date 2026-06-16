package com.restaurant.inventory.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.inventory.persistence.entity.CatalogRecipeIngredientEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:inventorycatalogrecipeingredientrepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=catalog",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class CatalogRecipeIngredientRepositoryTest {

    @Autowired
    private CatalogRecipeIngredientRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void findByMenuItemIdsReturnsMatchingRows() {
        insertRecipe("recipe-001", "item-001", "ing-001");
        insertRecipe("recipe-002", "item-002", "ing-002");

        assertThat(repository.findByMenuItemIdIn(List.of("item-001")))
                .extracting(CatalogRecipeIngredientEntity::getRecipeIngredientId)
                .containsExactly("recipe-001");
    }

    private void insertRecipe(String recipeIngredientId, String menuItemId, String ingredientId) {
        jdbcTemplate.update(
                """
                insert into catalog.recipe_ingredients
                (recipe_ingredient_id, menu_item_id, ingredient_id, quantity_required)
                values (?, ?, ?, ?)
                """,
                recipeIngredientId,
                menuItemId,
                ingredientId,
                1
        );
    }
}
