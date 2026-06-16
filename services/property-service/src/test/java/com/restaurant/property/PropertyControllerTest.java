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
class PropertyControllerTest {

    @Mock
    private PropertyService propertyService;

    private PropertyController controller;

    @BeforeEach
    void setUp() {
        controller = new PropertyController(propertyService);
    }

    @Test
    void listPropertiesPrefersPathScopedTenant() {
        List<PropertyResponse> expected = List.of(propertyResponse("krusty-krab"));
        when(propertyService.listProperties("tenant-path")).thenReturn(expected);

        List<PropertyResponse> response = controller.listProperties("tenant-path", "tenant-query");

        assertThat(response).isEqualTo(expected);
        verify(propertyService).listProperties("tenant-path");
    }

    @Test
    void createUpdateAndDeleteDelegateToService() {
        CreatePropertyRequest createRequest = new CreatePropertyRequest("Krusty Krab", "Ocean Avenue", "Bikini Bottom", "Ocean", "India", "Asia/Kolkata", 1.0, 2.0);
        UpdatePropertyRequest updateRequest = new UpdatePropertyRequest("Krusty Krab 2", "Ocean Avenue 2", "Bikini Bottom", "Ocean", "India", "Asia/Kolkata", 3.0, 4.0, "ACTIVE");
        PropertyResponse created = propertyResponse("krusty-krab");
        PropertyResponse updated = propertyResponse("krusty-krab");
        when(propertyService.createProperty("bikini-bottom", createRequest)).thenReturn(created);
        when(propertyService.updateProperty("bikini-bottom", "krusty-krab", updateRequest)).thenReturn(updated);

        assertThat(controller.createProperty(null, null, createRequest)).isEqualTo(created);
        assertThat(controller.updateProperty("krusty-krab", null, null, updateRequest)).isEqualTo(updated);
        controller.deleteProperty("krusty-krab", null, null);

        verify(propertyService).createProperty("bikini-bottom", createRequest);
        verify(propertyService).updateProperty("bikini-bottom", "krusty-krab", updateRequest);
        verify(propertyService).deleteProperty("bikini-bottom", "krusty-krab");
    }

    private PropertyResponse propertyResponse(String propertyId) {
        return new PropertyResponse("bikini-bottom", "chefy", propertyId, "Krusty Krab", "Bikini Bottom", "Ocean", "India", "Ocean Avenue", 1.0, 2.0, "ACTIVE");
    }
}
