package com.restaurant.audit;

import static org.mockito.Mockito.verify;

import com.restaurant.platform.eventing.DomainEventHandler;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InternalEventControllerTest {

    @Mock
    private DomainEventHandler firstHandler;

    @Mock
    private DomainEventHandler secondHandler;

    @Test
    void publishDelegatesRawEventToEveryHandler() {
        InternalEventController controller = new InternalEventController(List.of(firstHandler, secondHandler));

        controller.publish("{\"eventKey\":\"order.created\"}");

        verify(firstHandler).handle("{\"eventKey\":\"order.created\"}");
        verify(secondHandler).handle("{\"eventKey\":\"order.created\"}");
    }
}
