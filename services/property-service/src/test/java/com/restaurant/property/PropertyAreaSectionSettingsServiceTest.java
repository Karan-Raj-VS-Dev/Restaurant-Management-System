package com.restaurant.property;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.restaurant.property.persistence.entity.PropertyAreaSectionEntity;
import com.restaurant.property.persistence.repository.PropertyAreaSectionRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PropertyAreaSectionSettingsServiceTest {

    @Mock
    private PropertyAreaSectionRepository propertyAreaSectionRepository;

    private PropertyAreaSectionSettingsService service;

    @BeforeEach
    void setUp() {
        service = new PropertyAreaSectionSettingsService(propertyAreaSectionRepository);
    }

    @Test
    void createNormalizesIdAndJoinsDistinctNames() {
        when(propertyAreaSectionRepository.findByTenantIdAndPropertyIdAndAreaSectionId("bikini-bottom", "krusty-krab", "main-floor__dining"))
                .thenReturn(Optional.empty());
        when(propertyAreaSectionRepository.save(any(PropertyAreaSectionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AreaSectionSettingResponse response = service.create(
                "bikini-bottom",
                "krusty-krab",
                new UpsertAreaSectionSettingRequest("Main floor", "Dining", 12, List.of("Neha", "Neha", "Anu"), List.of("Pradeep"), "ACTIVE")
        );

        ArgumentCaptor<PropertyAreaSectionEntity> captor = ArgumentCaptor.forClass(PropertyAreaSectionEntity.class);
        verify(propertyAreaSectionRepository).save(captor.capture());
        PropertyAreaSectionEntity saved = captor.getValue();

        assertThat(saved.getAreaSectionId()).isEqualTo("main-floor__dining");
        assertThat(saved.getWaiterNames()).isEqualTo("Neha,Anu");
        assertThat(saved.getCleanerNames()).isEqualTo("Pradeep");
        assertThat(response.waiterNames()).containsExactly("Neha", "Anu");
    }

    @Test
    void getSummarySplitsNamesIntoLists() {
        PropertyAreaSectionEntity entity = new PropertyAreaSectionEntity();
        entity.setAreaSectionId("main-floor__dining");
        entity.setTenantId("bikini-bottom");
        entity.setPropertyId("krusty-krab");
        entity.setFloorName("Main floor");
        entity.setSectionName("Dining");
        entity.setMaxTableCount(12);
        entity.setWaiterNames("Neha,Anu");
        entity.setCleanerNames("Pradeep");
        entity.setStatus("ACTIVE");
        when(propertyAreaSectionRepository.findByTenantIdAndPropertyIdOrderByFloorNameAscSectionNameAsc("bikini-bottom", "krusty-krab"))
                .thenReturn(List.of(entity));

        AreaSectionSettingsSummaryResponse summary = service.getSummary("bikini-bottom", "krusty-krab");

        assertThat(summary.records()).singleElement()
                .extracting(AreaSectionSettingResponse::waiterNames, AreaSectionSettingResponse::cleanerNames)
                .containsExactly(List.of("Neha", "Anu"), List.of("Pradeep"));
    }

    @Test
    void updateExistingAreaSectionClearsBlankNamesIntoEmptyLists() {
        PropertyAreaSectionEntity entity = new PropertyAreaSectionEntity();
        entity.setAreaSectionId("main-floor__patio");
        entity.setTenantId("bikini-bottom");
        entity.setPropertyId("krusty-krab");
        entity.setFloorName("Main floor");
        entity.setSectionName("Patio");
        entity.setMaxTableCount(6);
        entity.setWaiterNames("Karthi");
        entity.setCleanerNames("Suresh");
        entity.setStatus("ACTIVE");

        when(propertyAreaSectionRepository.findByTenantIdAndPropertyIdAndAreaSectionId("bikini-bottom", "krusty-krab", "main-floor__patio"))
                .thenReturn(Optional.of(entity));
        when(propertyAreaSectionRepository.save(any(PropertyAreaSectionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AreaSectionSettingResponse response = service.update(
                "bikini-bottom",
                "krusty-krab",
                "main-floor__patio",
                new UpsertAreaSectionSettingRequest("Main floor", "Patio", 8, List.of("  ", ""), List.of(), "INACTIVE")
        );

        assertThat(entity.getMaxTableCount()).isEqualTo(8);
        assertThat(entity.getWaiterNames()).isEmpty();
        assertThat(entity.getCleanerNames()).isEmpty();
        assertThat(entity.getStatus()).isEqualTo("INACTIVE");
        assertThat(response.waiterNames()).isEmpty();
        assertThat(response.cleanerNames()).isEmpty();
    }

    @Test
    void seedDefaultsSkipsExistingAreaAndSavesOnlyMissingDefaults() {
        when(propertyAreaSectionRepository.existsByTenantIdAndPropertyIdAndAreaSectionId("bikini-bottom", "krusty-krab", "main-floor__dining"))
                .thenReturn(true);
        when(propertyAreaSectionRepository.existsByTenantIdAndPropertyIdAndAreaSectionId("bikini-bottom", "krusty-krab", "main-floor__patio"))
                .thenReturn(false);
        when(propertyAreaSectionRepository.save(any(PropertyAreaSectionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.seedDefaults();

        ArgumentCaptor<PropertyAreaSectionEntity> captor = ArgumentCaptor.forClass(PropertyAreaSectionEntity.class);
        verify(propertyAreaSectionRepository).save(captor.capture());
        assertThat(captor.getValue().getAreaSectionId()).isEqualTo("main-floor__patio");
        verify(propertyAreaSectionRepository).existsByTenantIdAndPropertyIdAndAreaSectionId("bikini-bottom", "krusty-krab", "main-floor__dining");
        verify(propertyAreaSectionRepository).existsByTenantIdAndPropertyIdAndAreaSectionId("bikini-bottom", "krusty-krab", "main-floor__patio");
    }
}
