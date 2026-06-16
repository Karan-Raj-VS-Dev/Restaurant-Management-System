package com.restaurant.property;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.restaurant.property.persistence.entity.PropertyEntity;
import com.restaurant.property.persistence.repository.PropertyRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    private PropertyService propertyService;

    @BeforeEach
    void setUp() {
        propertyService = new PropertyService(propertyRepository);
    }

    @Test
    void getPropertyReturnsFallbackWhenUnknown() {
        when(propertyRepository.findByTenantIdAndPropertyId("bikini-bottom", "unknown")).thenReturn(Optional.empty());

        PropertyResponse response = propertyService.getProperty("bikini-bottom", "unknown");

        assertThat(response.name()).isEqualTo("Unknown Property");
        assertThat(response.status()).isEqualTo("UNKNOWN");
    }

    @Test
    void createPropertyRejectsDuplicateName() {
        when(propertyRepository.existsByTenantIdAndPropertyNameIgnoreCase("bikini-bottom", "Krusty Krab")).thenReturn(true);

        assertThatThrownBy(() -> propertyService.createProperty(
                "bikini-bottom",
                new CreatePropertyRequest("Krusty Krab", "Ocean Avenue", "Bikini Bottom", "Ocean", "India", "Asia/Kolkata", null, null)
        )).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Property name is already in use");
    }

    @Test
    void createPropertyAppliesDefaultsAndPersistsEntity() {
        when(propertyRepository.existsByTenantIdAndPropertyNameIgnoreCase("bikini-bottom", "Krusty Krab")).thenReturn(false);
        when(propertyRepository.save(any(PropertyEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PropertyResponse response = propertyService.createProperty(
                "bikini-bottom",
                new CreatePropertyRequest("Krusty Krab", "Ocean Avenue", "Bikini Bottom", null, null, null, 1.0, 2.0)
        );

        ArgumentCaptor<PropertyEntity> captor = ArgumentCaptor.forClass(PropertyEntity.class);
        verify(propertyRepository).save(captor.capture());
        PropertyEntity saved = captor.getValue();

        assertThat(saved.getCountry()).isEqualTo("India");
        assertThat(saved.getTimezone()).isEqualTo("Asia/Kolkata");
        assertThat(saved.getStatus()).isEqualTo("ACTIVE");
        assertThat(response.name()).isEqualTo("Krusty Krab");
    }

    @Test
    void updatePropertyChangesSuppliedFields() {
        PropertyEntity entity = property("krusty-krab", "Krusty Krab");
        when(propertyRepository.findByTenantIdAndPropertyId("bikini-bottom", "krusty-krab")).thenReturn(Optional.of(entity));
        when(propertyRepository.save(any(PropertyEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PropertyResponse response = propertyService.updateProperty(
                "bikini-bottom",
                "krusty-krab",
                new UpdatePropertyRequest("Krusty Krab Two", "Ocean Avenue 2", "New City", "New State", "India", "UTC", 10.0, 20.0, "inactive")
        );

        assertThat(response.name()).isEqualTo("Krusty Krab Two");
        assertThat(entity.getCity()).isEqualTo("New City");
        assertThat(entity.getStatus()).isEqualTo("INACTIVE");
    }

    @Test
    void deletePropertyRemovesEntity() {
        PropertyEntity entity = property("krusty-krab", "Krusty Krab");
        when(propertyRepository.findByTenantIdAndPropertyId("bikini-bottom", "krusty-krab")).thenReturn(Optional.of(entity));

        propertyService.deleteProperty("bikini-bottom", "krusty-krab");

        verify(propertyRepository).delete(entity);
    }

    private PropertyEntity property(String propertyId, String name) {
        PropertyEntity entity = new PropertyEntity();
        entity.setPropertyId(propertyId);
        entity.setTenantId("bikini-bottom");
        entity.setPropertyCode("BB-KK-0001");
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
