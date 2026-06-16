package com.restaurant.eventgateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventGatewayControllerTest {

    @Mock
    private EventDispatchService eventDispatchService;

    @Test
    void publishDelegatesRawEventToDispatchService() {
        EventGatewayController controller = new EventGatewayController(eventDispatchService);
        EventDispatchResponse response = new EventDispatchResponse(2, 1);
        when(eventDispatchService.publish("{\"eventKey\":\"order.created\"}")).thenReturn(response);

        EventDispatchResponse result = controller.publish("{\"eventKey\":\"order.created\"}");

        assertThat(result).isEqualTo(response);
        verify(eventDispatchService).publish("{\"eventKey\":\"order.created\"}");
    }
}
