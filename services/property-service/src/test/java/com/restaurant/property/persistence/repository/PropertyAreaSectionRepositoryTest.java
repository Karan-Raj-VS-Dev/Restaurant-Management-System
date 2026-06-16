package com.restaurant.property.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.property.persistence.entity.PropertyAreaSectionEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:propertyarearepo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=property",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class PropertyAreaSectionRepositoryTest {

    @Autowired
    private PropertyAreaSectionRepository repository;

    @Test
    void findByTenantPropertyAndAreaSectionIdReturnsMatchingRecord() {
        repository.save(areaSection("main-floor__dining", "Main floor", "Dining"));

        assertThat(repository.findByTenantIdAndPropertyIdAndAreaSectionId("bikini-bottom", "krusty-krab", "main-floor__dining"))
                .get()
                .extracting(PropertyAreaSectionEntity::getSectionName)
                .isEqualTo("Dining");
    }

    @Test
    void orderedLookupSortsByFloorAndSection() {
        repository.saveAll(List.of(
                areaSection("main-floor__patio", "Main floor", "Patio"),
                areaSection("main-floor__dining", "Main floor", "Dining")
        ));

        List<PropertyAreaSectionEntity> results = repository.findByTenantIdAndPropertyIdOrderByFloorNameAscSectionNameAsc("bikini-bottom", "krusty-krab");

        assertThat(results).extracting(PropertyAreaSectionEntity::getAreaSectionId).containsExactly("main-floor__dining", "main-floor__patio");
    }

    private PropertyAreaSectionEntity areaSection(String id, String floorName, String sectionName) {
        PropertyAreaSectionEntity entity = new PropertyAreaSectionEntity();
        entity.setAreaSectionId(id);
        entity.setTenantId("bikini-bottom");
        entity.setPropertyId("krusty-krab");
        entity.setFloorName(floorName);
        entity.setSectionName(sectionName);
        entity.setMaxTableCount(10);
        entity.setWaiterNames("Neha,Anu");
        entity.setCleanerNames("Pradeep");
        entity.setStatus("ACTIVE");
        return entity;
    }
}
