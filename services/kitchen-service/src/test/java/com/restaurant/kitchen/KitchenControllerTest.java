package com.restaurant.kitchen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KitchenControllerTest {

    @Mock
    private KitchenService kitchenService;

    private KitchenController controller;

    @BeforeEach
    void setUp() {
        controller = new KitchenController(kitchenService);
    }

    @Test
    void listTicketsUsesDefaultScope() {
        List<KitchenTicketResponse> expected = List.of(ticket("ticket-001", KitchenStatus.RECEIVED));
        when(kitchenService.listTickets("bikini-bottom", "krusty-krab")).thenReturn(expected);

        List<KitchenTicketResponse> response = controller.listTickets(null, null, null, null);

        assertThat(response).isEqualTo(expected);
        verify(kitchenService).listTickets("bikini-bottom", "krusty-krab");
    }

    @Test
    void createAndStatusEndpointsDelegateToService() {
        CreateKitchenTicketRequest createRequest = new CreateKitchenTicketRequest("order-001", "krusty-krab", "cook-001");
        UpdateKitchenTicketRequest updateRequest = new UpdateKitchenTicketRequest("cook-001", "burned", "ticket-002");
        KitchenTicketResponse received = ticket("ticket-001", KitchenStatus.RECEIVED);
        KitchenTicketResponse accepted = ticket("ticket-001", KitchenStatus.ACCEPTED);
        KitchenTicketResponse ready = ticket("ticket-001", KitchenStatus.READY);
        KitchenTicketResponse served = ticket("ticket-001", KitchenStatus.SERVED);
        KitchenTicketResponse cancelled = ticket("ticket-001", KitchenStatus.CANCELLED);
        KitchenTicketResponse dumped = ticket("ticket-001", KitchenStatus.DUMPED);
        KitchenTicketResponse reused = ticket("ticket-001", KitchenStatus.REUSED);
        when(kitchenService.createTicket("bikini-bottom", "krusty-krab", createRequest)).thenReturn(received);
        when(kitchenService.acceptTicket("bikini-bottom", "krusty-krab", "ticket-001", updateRequest)).thenReturn(accepted);
        when(kitchenService.markReady("bikini-bottom", "krusty-krab", "ticket-001", updateRequest)).thenReturn(ready);
        when(kitchenService.markServed("bikini-bottom", "krusty-krab", "ticket-001")).thenReturn(served);
        when(kitchenService.markCancelled("bikini-bottom", "krusty-krab", "ticket-001", updateRequest)).thenReturn(cancelled);
        when(kitchenService.markDumped("bikini-bottom", "krusty-krab", "ticket-001")).thenReturn(dumped);
        when(kitchenService.markReused("bikini-bottom", "krusty-krab", "ticket-001", updateRequest)).thenReturn(reused);

        assertThat(controller.createTicket(null, null, null, null, createRequest)).isEqualTo(received);
        assertThat(controller.acceptTicket("ticket-001", null, null, null, null, updateRequest)).isEqualTo(accepted);
        assertThat(controller.markReady("ticket-001", null, null, null, null, updateRequest)).isEqualTo(ready);
        assertThat(controller.markServed("ticket-001", null, null, null, null)).isEqualTo(served);
        assertThat(controller.markCancelled("ticket-001", null, null, null, null, updateRequest)).isEqualTo(cancelled);
        assertThat(controller.markDumped("ticket-001", null, null, null, null)).isEqualTo(dumped);
        assertThat(controller.markReused("ticket-001", null, null, null, null, updateRequest)).isEqualTo(reused);
    }

    private KitchenTicketResponse ticket(String ticketId, KitchenStatus status) {
        return new KitchenTicketResponse(ticketId, "order-001", "krusty-krab", "cook-001", status, Instant.parse("2026-06-15T10:00:00Z"), null, null);
    }
}
