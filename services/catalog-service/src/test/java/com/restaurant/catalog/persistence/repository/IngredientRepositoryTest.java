package com.restaurant.catalog.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.catalog.persistence.entity.IngredientEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:ingredientrepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=catalog",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class IngredientRepositoryTest {

    @Autowired
    private IngredientRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void findByTenantPropertyAndIngredientIdReturnsMatch() {
        insertIngredient("ing-001", "Cream");

        assertThat(repository.findByTenantIdAndPropertyIdAndIngredientId("bikini-bottom", "krusty-krab", "ing-001"))
                .get()
                .extracting(IngredientEntity::getIngredientName)
                .isEqualTo("Cream");
    }

    @Test
    void findByTenantPropertyAndIngredientIdsReturnsRequestedSubset() {
        insertIngredient("ing-001", "Cream");
        insertIngredient("ing-002", "Pasta");
        insertIngredient("ing-003", "Mozzarella");

        assertThat(repository.findByTenantIdAndPropertyIdAndIngredientIdIn("bikini-bottom", "krusty-krab", List.of("ing-001", "ing-003")))
                .extracting(IngredientEntity::getIngredientId)
                .containsExactlyInAnyOrder("ing-001", "ing-003");
    }

    private void insertIngredient(String ingredientId, String name) {
        jdbcTemplate.update(
                """
                insert into catalog.ingredients
                (ingredient_id, tenant_id, property_id, ingredient_code, ingredient_name, unit_of_measure, is_active, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, current_timestamp, current_timestamp)
                """,
                ingredientId,
                "bikini-bottom",
                "krusty-krab",
                ingredientId.toUpperCase(),
                name,
                "grams",
                true
        );
    }
}
