package com.restaurant.eventgateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/events")
public class EventGatewayController {

    private final EventDispatchService eventDispatchService;

    public EventGatewayController(EventDispatchService eventDispatchService) {
        this.eventDispatchService = eventDispatchService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public EventDispatchResponse publish(@RequestBody String rawEvent) {
        return eventDispatchService.publish(rawEvent);
    }
}
