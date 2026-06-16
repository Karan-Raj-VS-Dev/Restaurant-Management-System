package com.restaurant.kitchen;

import static org.mockito.Mockito.verify;

import com.restaurant.platform.eventing.DomainEventHandler;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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

    private InternalEventController controller;

    @BeforeEach
    void setUp() {
        controller = new InternalEventController(List.of(firstHandler, secondHandler));
    }

    @Test
    void publishFansOutRawMessageToAllHandlers() {
        controller.publish("{\"event\":\"ticket.created\"}");

        verify(firstHandler).handle("{\"event\":\"ticket.created\"}");
        verify(secondHandler).handle("{\"event\":\"ticket.created\"}");
    }
}
