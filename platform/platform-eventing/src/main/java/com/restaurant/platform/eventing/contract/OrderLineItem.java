package com.restaurant.platform.eventing.contract;

public record OrderLineItem(
        String itemId,
        String itemName,
        int quantity
) {
}
