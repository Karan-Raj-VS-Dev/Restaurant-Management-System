package com.restaurant.property.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.property.persistence.entity.PropertyEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:propertyrepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=property",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class PropertyRepositoryTest {

    @Autowired
    private PropertyRepository propertyRepository;

    @Test
    void findByTenantIdOrderByPropertyNameAscSortsResults() {
        propertyRepository.saveAll(List.of(
                property("kk", "Krusty Krab"),
                property("cb", "Chum Bucket")
        ));

        List<PropertyEntity> properties = propertyRepository.findByTenantIdOrderByPropertyNameAsc("bikini-bottom");

        assertThat(properties).extracting(PropertyEntity::getPropertyId).containsExactly("cb", "kk");
    }

    @Test
    void existsByTenantIdAndPropertyNameIgnoreCaseMatchesIgnoringCase() {
        propertyRepository.save(property("kk", "Krusty Krab"));

        assertThat(propertyRepository.existsByTenantIdAndPropertyNameIgnoreCase("bikini-bottom", "krusty krab")).isTrue();
    }

    private PropertyEntity property(String propertyId, String name) {
        PropertyEntity entity = new PropertyEntity();
        entity.setPropertyId(propertyId);
        entity.setTenantId("bikini-bottom");
        entity.setPropertyCode("BB-" + propertyId.toUpperCase());
        entity.setPropertyName(name);
        entity.setAddressLine("Ocean Avenue");
        entity.setCity("Bikini Bottom");
        entity.setState("Ocean");
        entity.setCountry("India");
        entity.setTimezone("Asia/Kolkata");
        entity.setStatus("ACTIVE");
        return entity;
    }
}
