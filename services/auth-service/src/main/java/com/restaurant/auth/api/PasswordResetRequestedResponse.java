package com.restaurant.auth.api;

public record PasswordResetRequestedResponse(
        boolean accountFound,
        String message,
        String deliveryChannel,
        String deliveryHint,
        String devOtp
) {
}
