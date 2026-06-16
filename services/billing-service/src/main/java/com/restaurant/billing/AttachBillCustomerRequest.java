package com.restaurant.billing;

import jakarta.validation.constraints.NotBlank;

public record AttachBillCustomerRequest(@NotBlank String customerId) {
}
