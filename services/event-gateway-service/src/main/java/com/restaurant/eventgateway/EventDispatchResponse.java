package com.restaurant.eventgateway;

public record EventDispatchResponse(
        int delivered,
        int failed
) {
}
