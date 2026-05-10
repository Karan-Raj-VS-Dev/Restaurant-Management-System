package com.restaurant.review;

import com.restaurant.platform.eventing.DomainEventHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/internal/events")
public class InternalEventController {

    private final List<DomainEventHandler> handlers;

    public InternalEventController(List<DomainEventHandler> handlers) {
        this.handlers = handlers;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void publish(@RequestBody String rawMessage) {
        handlers.forEach(handler -> handler.handle(rawMessage));
    }
}
