package com.restaurant.inventory.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.inventory.persistence.entity.OperationalSupplyEntity;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:operationalsupplyrepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=inventory",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class OperationalSupplyRepositoryTest {

    @Autowired
    private OperationalSupplyRepository repository;

    @Test
    void findByTenantPropertyOrdersBySupplyName() {
        repository.saveAll(List.of(
                supply("sup-002", "Wipes"),
                supply("sup-001", "Aprons")
        ));

        assertThat(repository.findByTenantIdAndPropertyIdOrderBySupplyNameAsc("bikini-bottom", "krusty-krab"))
                .extracting(OperationalSupplyEntity::getSupplyName)
                .containsExactly("Aprons", "Wipes");
    }

    @Test
    void findByTenantPropertyAndSupplyIdReturnsMatch() {
        repository.save(supply("sup-001", "Aprons"));

        assertThat(repository.findByTenantIdAndPropertyIdAndSupplyId("bikini-bottom", "krusty-krab", "sup-001")).isPresent();
    }

    private OperationalSupplyEntity supply(String supplyId, String name) {
        OperationalSupplyEntity entity = new OperationalSupplyEntity();
        entity.setSupplyId(supplyId);
        entity.setTenantId("bikini-bottom");
        entity.setPropertyId("krusty-krab");
        entity.setSupplyCode(supplyId.toUpperCase());
        entity.setSupplyName(name);
        entity.setUnitOfMeasure("pieces");
        entity.setReorderLevel(BigDecimal.valueOf(3));
        entity.setMarketUnitPrice(BigDecimal.valueOf(10));
        entity.setStatus("ACTIVE");
        return entity;
    }
}
