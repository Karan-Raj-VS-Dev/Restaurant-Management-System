package com.restaurant.billing.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.billing.persistence.entity.TaxDefinitionEntity;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:taxdefinitionrepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=billing",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class TaxDefinitionRepositoryTest {

    @Autowired
    private TaxDefinitionRepository repository;

    @Test
    void findByTenantPropertyOrdersByTaxName() {
        repository.save(TaxDefinitionEntity.create("tax-002", "bikini-bottom", "krusty-krab", "VAT", BigDecimal.valueOf(12), "BILL", "ACTIVE"));
        repository.save(TaxDefinitionEntity.create("tax-001", "bikini-bottom", "krusty-krab", "GST", BigDecimal.valueOf(5), "BILL", "ACTIVE"));

        assertThat(repository.findByTenantIdAndPropertyIdOrderByTaxNameAsc("bikini-bottom", "krusty-krab"))
                .extracting(TaxDefinitionEntity::getTaxName)
                .containsExactly("GST", "VAT");
    }
}
