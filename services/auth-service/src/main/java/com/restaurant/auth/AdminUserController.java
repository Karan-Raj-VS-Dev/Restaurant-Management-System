package com.restaurant.auth;

import com.restaurant.auth.api.ManagedUserRequest;
import com.restaurant.auth.api.ManagedUserResponse;
import com.restaurant.auth.api.ManagedUserUpdateRequest;
import com.restaurant.auth.api.ManagedUsersResponse;
import com.restaurant.auth.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({
        "/api/auth/admin/users",
        "/chefy/tenant/{tenantId}/api/auth/admin/users",
        "/chefy/tenant/{tenantId}/property/{propertyId}/api/auth/admin/users"
})
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ManagedUsersResponse listUsers(@AuthenticationPrincipal Jwt jwt) {
        return adminUserService.listUsers(jwt);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ManagedUserResponse createUser(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ManagedUserRequest request
    ) {
        return adminUserService.createUser(jwt, request);
    }

    @PutMapping("/{userId}")
    public ManagedUserResponse updateUser(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String userId,
            @Valid @RequestBody ManagedUserUpdateRequest request
    ) {
        return adminUserService.updateUser(jwt, userId, request);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String userId
    ) {
        adminUserService.deleteUser(jwt, userId);
    }
}
