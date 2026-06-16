package com.restaurant.customer;

public record CustomerLookupResponse(
        boolean existing,
        String customerId,
        String name,
        String phoneNumber
) {
}
