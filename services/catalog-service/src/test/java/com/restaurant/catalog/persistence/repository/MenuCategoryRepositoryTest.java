package com.restaurant.catalog.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.catalog.persistence.entity.MenuCategoryEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:menucategoryrepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=catalog",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class MenuCategoryRepositoryTest {

    @Autowired
    private MenuCategoryRepository repository;

    @Test
    void findByNameIsCaseInsensitive() {
        repository.save(MenuCategoryEntity.create("cat-001", "bikini-bottom", "krusty-krab", "Italian dishes", 1, true));

        assertThat(repository.findByTenantIdAndPropertyIdAndCategoryNameIgnoreCase("bikini-bottom", "krusty-krab", "ITALIAN DISHES")).isPresent();
    }

    @Test
    void activeCategoriesAreOrderedByDisplayOrderThenName() {
        repository.saveAll(List.of(
                MenuCategoryEntity.create("cat-002", "bikini-bottom", "krusty-krab", "Starters", 2, true),
                MenuCategoryEntity.create("cat-003", "bikini-bottom", "krusty-krab", "Desserts", 2, true),
                MenuCategoryEntity.create("cat-001", "bikini-bottom", "krusty-krab", "Italian dishes", 1, true),
                MenuCategoryEntity.create("cat-004", "bikini-bottom", "krusty-krab", "Archived", 3, false)
        ));

        assertThat(repository.findByTenantIdAndPropertyIdAndActiveTrueOrderByDisplayOrderAscCategoryNameAsc("bikini-bottom", "krusty-krab"))
                .extracting(MenuCategoryEntity::getCategoryName)
                .containsExactly("Italian dishes", "Desserts", "Starters");
    }
}
