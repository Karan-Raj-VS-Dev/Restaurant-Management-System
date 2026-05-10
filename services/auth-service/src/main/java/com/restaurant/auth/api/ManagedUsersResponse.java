package com.restaurant.auth.api;

import java.util.List;

public record ManagedUsersResponse(
        long totalUsers,
        List<ManagedUserResponse> users
) {
}
