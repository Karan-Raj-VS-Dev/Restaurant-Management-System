package com.restaurant.takeaway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TakeawayControllerTest {

    @Mock
    private TakeawayService takeawayService;

    private TakeawayController controller;

    @BeforeEach
    void setUp() {
        controller = new TakeawayController(takeawayService);
    }

    @Test
    void createTakeawayOrderUsesRequestPropertyWhenQueryMissing() {
        CreateTakeawayOrderRequest request = new CreateTakeawayOrderRequest("krusty-krab", "DIRECT");
        TakeawayOrderResponse expected = new TakeawayOrderResponse("to-001", "bikini-bottom", "krusty-krab", "DIRECT", TakeawayStatus.CREATED, Instant.parse("2026-06-15T10:00:00Z"));
        when(takeawayService.createTakeawayOrder("bikini-bottom", "krusty-krab", request)).thenReturn(expected);

        TakeawayOrderResponse response = controller.createTakeawayOrder(null, null, null, null, request);

        assertThat(response).isEqualTo(expected);
        verify(takeawayService).createTakeawayOrder("bikini-bottom", "krusty-krab", request);
    }

    @Test
    void getTakeawayOrderPrefersPathScope() {
        TakeawayOrderResponse expected = new TakeawayOrderResponse("to-001", "tenant-path", "property-path", "DIRECT", TakeawayStatus.CREATED, Instant.parse("2026-06-15T10:00:00Z"));
        when(takeawayService.getTakeawayOrder("tenant-path", "property-path", "to-001")).thenReturn(expected);

        TakeawayOrderResponse response = controller.getTakeawayOrder("to-001", "tenant-path", "property-path", "tenant-query", "property-query");

        assertThat(response).isEqualTo(expected);
        verify(takeawayService).getTakeawayOrder("tenant-path", "property-path", "to-001");
    }
}
