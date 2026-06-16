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
class PropertySettingsControllerTest {

    @Mock
    private PropertySettingsService propertySettingsService;

    private PropertySettingsController controller;

    @BeforeEach
    void setUp() {
        controller = new PropertySettingsController(propertySettingsService);
    }

    @Test
    void getOverviewUsesPathScopeFirst() {
        PropertySettingsOverviewResponse expected = new PropertySettingsOverviewResponse(
                "tenant-path",
                "property-path",
                List.of(),
                List.of()
        );
        when(propertySettingsService.getOverview("tenant-path", "property-path")).thenReturn(expected);

        PropertySettingsOverviewResponse response = controller.getOverview("tenant-path", "property-path", "tenant-query", "property-query");

        assertThat(response).isEqualTo(expected);
        verify(propertySettingsService).getOverview("tenant-path", "property-path");
    }
}
