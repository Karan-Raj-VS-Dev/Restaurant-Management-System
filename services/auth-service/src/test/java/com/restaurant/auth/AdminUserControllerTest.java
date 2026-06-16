package com.restaurant.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.restaurant.auth.api.ManagedUserRequest;
import com.restaurant.auth.api.ManagedUserResponse;
import com.restaurant.auth.api.ManagedUserUpdateRequest;
import com.restaurant.auth.api.ManagedUsersResponse;
import com.restaurant.auth.service.AdminUserService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    private AdminUserService adminUserService;

    private AdminUserController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminUserController(adminUserService);
    }

    @Test
    void listUsersDelegatesToService() {
        Jwt jwt = jwt();
        ManagedUsersResponse expected = new ManagedUsersResponse(1, List.of(userResponse("usr-001", "KaranRaj")));
        when(adminUserService.listUsers(jwt)).thenReturn(expected);

        ManagedUsersResponse response = controller.listUsers(jwt);

        assertThat(response).isEqualTo(expected);
        verify(adminUserService).listUsers(jwt);
    }

    @Test
    void createUserDelegatesToService() {
        Jwt jwt = jwt();
        ManagedUserRequest request = new ManagedUserRequest(
                "Karan",
                "Raj",
                "KaranRaj",
                "Temp@1234",
                "+91",
                "8901913123",
                "karan@restaurant.local",
                "Bikini Bottom",
                List.of("krusty-krab"),
                9.87,
                10.45
        );
        ManagedUserResponse expected = userResponse("usr-002", "KaranRaj");
        when(adminUserService.createUser(jwt, request)).thenReturn(expected);

        ManagedUserResponse response = controller.createUser(jwt, request);

        assertThat(response).isEqualTo(expected);
        verify(adminUserService).createUser(jwt, request);
    }

    @Test
    void updateUserDelegatesToService() {
        Jwt jwt = jwt();
        ManagedUserUpdateRequest request = new ManagedUserUpdateRequest(
                "Updated",
                "User",
                "updatedUser",
                "Another@123",
                "+91",
                "9000000000",
                "updated@restaurant.local",
                "Updated address",
                List.of("krusty-krab"),
                12.3,
                45.6,
                "ACTIVE"
        );
        ManagedUserResponse expected = userResponse("usr-003", "updatedUser");
        when(adminUserService.updateUser(jwt, "usr-003", request)).thenReturn(expected);

        ManagedUserResponse response = controller.updateUser(jwt, "usr-003", request);

        assertThat(response).isEqualTo(expected);
        verify(adminUserService).updateUser(jwt, "usr-003", request);
    }

    @Test
    void deleteUserDelegatesToService() {
        Jwt jwt = jwt();

        controller.deleteUser(jwt, "usr-004");

        verify(adminUserService).deleteUser(jwt, "usr-004");
    }

    private Jwt jwt() {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("usr-admin")
                .claim("preferred_username", "kingChef")
                .build();
    }

    private ManagedUserResponse userResponse(String userId, String username) {
        return new ManagedUserResponse(
                userId,
                "bikini-bottom",
                null,
                List.of("krusty-krab"),
                "Karan",
                "Raj",
                "Karan Raj",
                username,
                username + "@restaurant.local",
                "+91",
                "8901913123",
                "Address",
                10.0,
                20.0,
                "ACTIVE",
                false,
                true,
                Instant.parse("2026-06-15T08:00:00Z"),
                Instant.parse("2026-06-15T07:00:00Z")
        );
    }
}
