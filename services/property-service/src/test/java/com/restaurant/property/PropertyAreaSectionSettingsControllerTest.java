package com.restaurant.property;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PropertyAreaSectionSettingsControllerTest {

    @Mock
    private PropertyAreaSectionSettingsService propertyAreaSectionSettingsService;

    private PropertyAreaSectionSettingsController controller;

    @BeforeEach
    void setUp() {
        controller = new PropertyAreaSectionSettingsController(propertyAreaSectionSettingsService);
    }

    @Test
    void summaryUsesDefaultScope() {
        AreaSectionSettingsSummaryResponse expected = new AreaSectionSettingsSummaryResponse(List.of("floor"), List.of());
        when(propertyAreaSectionSettingsService.getSummary("bikini-bottom", "krusty-krab")).thenReturn(expected);

        AreaSectionSettingsSummaryResponse response = controller.getSummary(null, null, null, null);

        assertThat(response).isEqualTo(expected);
        verify(propertyAreaSectionSettingsService).getSummary("bikini-bottom", "krusty-krab");
    }

    @Test
    void createAndUpdateDelegateToService() {
        UpsertAreaSectionSettingRequest request = new UpsertAreaSectionSettingRequest("Main floor", "Dining", 10, List.of("Neha"), List.of("Pradeep"), "ACTIVE");
        AreaSectionSettingResponse created = new AreaSectionSettingResponse("main-floor__dining", "Main floor", "Dining", 10, List.of("Neha"), List.of("Pradeep"), "ACTIVE");
        when(propertyAreaSectionSettingsService.create("bikini-bottom", "krusty-krab", request)).thenReturn(created);
        when(propertyAreaSectionSettingsService.update("bikini-bottom", "krusty-krab", "main-floor__dining", request)).thenReturn(created);

        assertThat(controller.create(null, null, null, null, request)).isEqualTo(created);
        assertThat(controller.update("main-floor__dining", null, null, null, null, request)).isEqualTo(created);

        verify(propertyAreaSectionSettingsService).create("bikini-bottom", "krusty-krab", request);
        verify(propertyAreaSectionSettingsService).update("bikini-bottom", "krusty-krab", "main-floor__dining", request);
    }
}
