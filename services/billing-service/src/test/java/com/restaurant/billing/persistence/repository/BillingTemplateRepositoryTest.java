package com.restaurant.billing.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.billing.persistence.entity.BillingTemplateEntity;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:billingtemplaterepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=billing",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class BillingTemplateRepositoryTest {

    @Autowired
    private BillingTemplateRepository repository;

    @Test
    void findByTenantPropertyOrdersByTemplateName() {
        repository.saveAll(List.of(
                BillingTemplateEntity.create("tpl-002", "bikini-bottom", "krusty-krab", "Zeta", Map.of("summary", "Z"), "ACTIVE"),
                BillingTemplateEntity.create("tpl-001", "bikini-bottom", "krusty-krab", "Alpha", Map.of("summary", "A"), "ACTIVE")
        ));

        assertThat(repository.findByTenantIdAndPropertyIdOrderByTemplateNameAsc("bikini-bottom", "krusty-krab"))
                .extracting(BillingTemplateEntity::getTemplateName)
                .containsExactly("Alpha", "Zeta");
    }
}
