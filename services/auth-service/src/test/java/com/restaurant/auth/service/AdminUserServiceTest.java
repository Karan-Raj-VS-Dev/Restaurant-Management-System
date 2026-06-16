package com.restaurant.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.restaurant.auth.api.ManagedUserRequest;
import com.restaurant.auth.api.ManagedUserUpdateRequest;
import com.restaurant.auth.persistence.entity.AppUserEntity;
import com.restaurant.auth.persistence.entity.UserPropertyAccessEntity;
import com.restaurant.auth.persistence.repository.AppUserRepository;
import com.restaurant.auth.persistence.repository.UserPropertyAccessRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private UserPropertyAccessRepository userPropertyAccessRepository;

    private AdminUserService adminUserService;

    @BeforeEach
    void setUp() {
        adminUserService = new AdminUserService(appUserRepository, userPropertyAccessRepository);
    }

    @Test
    void listUsersReturnsTenantScopedUsers() {
        AppUserEntity admin = adminUser();
        AppUserEntity user = employeeUser("usr-101", "sponge");
        when(appUserRepository.findByUsernameIgnoreCase("kingChef")).thenReturn(Optional.of(admin));
        when(appUserRepository.findAllByTenantIdOrderByCreatedAtDesc("bikini-bottom")).thenReturn(List.of(user));
        when(userPropertyAccessRepository.findByUserIdOrderByCreatedAtAsc("usr-101"))
                .thenReturn(List.of(mapping("usr-101", "krusty-krab")));

        var response = adminUserService.listUsers(adminJwt());

        assertThat(response.totalUsers()).isEqualTo(1);
        assertThat(response.users()).extracting(userResponse -> userResponse.username()).containsExactly("sponge");
    }

    @Test
    void createUserSavesUserAndPropertyMappings() {
        AppUserEntity admin = adminUser();
        when(appUserRepository.findByUsernameIgnoreCase("kingChef")).thenReturn(Optional.of(admin));
        when(appUserRepository.findByUsernameIgnoreCase("patrick")).thenReturn(Optional.empty());
        when(appUserRepository.findByEmailIgnoreCase("patrick@restaurant.local")).thenReturn(Optional.empty());
        when(appUserRepository.findByPhoneE164("+919000000001")).thenReturn(Optional.empty());
        when(appUserRepository.save(any(AppUserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userPropertyAccessRepository.findByUserIdOrderByCreatedAtAsc(any())).thenReturn(List.of(mapping("generated-user", "krusty-krab")));

        var response = adminUserService.createUser(adminJwt(), new ManagedUserRequest(
                "Patrick",
                "Star",
                "patrick",
                "Temp@1234",
                "+91",
                "9000000001",
                "patrick@restaurant.local",
                "Bikini Bottom",
                List.of("krusty-krab"),
                1.23,
                4.56
        ));

        assertThat(response.username()).isEqualTo("patrick");
        verify(userPropertyAccessRepository).deleteByUserId(any());
        verify(userPropertyAccessRepository).flush();
        verify(userPropertyAccessRepository).saveAndFlush(any(UserPropertyAccessEntity.class));
    }

    @Test
    void updateUserResetsPasswordAndSyncsMappings() {
        AppUserEntity admin = adminUser();
        AppUserEntity existing = employeeUser("usr-102", "squidward");
        when(appUserRepository.findByUsernameIgnoreCase("kingChef")).thenReturn(Optional.of(admin));
        when(appUserRepository.findById("usr-102")).thenReturn(Optional.of(existing));
        when(appUserRepository.findByUsernameIgnoreCase("updated-user")).thenReturn(Optional.empty());
        when(appUserRepository.findByEmailIgnoreCase("updated@restaurant.local")).thenReturn(Optional.empty());
        when(appUserRepository.findByPhoneE164("+919111111111")).thenReturn(Optional.empty());
        when(appUserRepository.save(any(AppUserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userPropertyAccessRepository.findByUserIdOrderByCreatedAtAsc("usr-102"))
                .thenReturn(List.of(mapping("usr-102", "krusty-krab")));

        var response = adminUserService.updateUser(adminJwt(), "usr-102", new ManagedUserUpdateRequest(
                "Updated",
                "User",
                "updated-user",
                "Another@123",
                "+91",
                "9111111111",
                "updated@restaurant.local",
                "Updated address",
                List.of("krusty-krab"),
                11.1,
                22.2,
                "ACTIVE"
        ));

        assertThat(response.username()).isEqualTo("updated-user");
        assertThat(existing.getPassword()).isEqualTo("Another@123");
        assertThat(existing.isMustChangePassword()).isTrue();
        verify(userPropertyAccessRepository).deleteByUserId("usr-102");
    }

    @Test
    void deleteUserRemovesMappingsAndUser() {
        AppUserEntity admin = adminUser();
        AppUserEntity existing = employeeUser("usr-103", "gary");
        when(appUserRepository.findByUsernameIgnoreCase("kingChef")).thenReturn(Optional.of(admin));
        when(appUserRepository.findById("usr-103")).thenReturn(Optional.of(existing));

        adminUserService.deleteUser(adminJwt(), "usr-103");

        verify(userPropertyAccessRepository).deleteByUserId("usr-103");
        verify(appUserRepository).delete(existing);
    }

    @Test
    void createUserRejectsDuplicateUsername() {
        AppUserEntity admin = adminUser();
        when(appUserRepository.findByUsernameIgnoreCase("kingChef")).thenReturn(Optional.of(admin));
        when(appUserRepository.findByUsernameIgnoreCase("patrick")).thenReturn(Optional.of(employeeUser("usr-200", "patrick")));

        assertThatThrownBy(() -> adminUserService.createUser(adminJwt(), new ManagedUserRequest(
                "Patrick",
                "Star",
                "patrick",
                "Temp@1234",
                "+91",
                "9000000001",
                "patrick@restaurant.local",
                "Bikini Bottom",
                List.of("krusty-krab"),
                null,
                null
        )))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.CONFLICT);

        verify(appUserRepository, never()).save(any());
    }

    private Jwt adminJwt() {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("usr-admin")
                .claim("preferred_username", "kingChef")
                .build();
    }

    private AppUserEntity adminUser() {
        AppUserEntity entity = employeeUser("usr-admin", "kingChef");
        entity.setAdminUser(true);
        entity.setStatus("ACTIVE");
        return entity;
    }

    private AppUserEntity employeeUser(String userId, String username) {
        AppUserEntity entity = new AppUserEntity();
        entity.setUserId(userId);
        entity.setTenantId("bikini-bottom");
        entity.setUsername(username);
        entity.setEmail(username + "@restaurant.local");
        entity.setPhoneCountryCode("+91");
        entity.setPhoneNumber("9000000000");
        entity.setPhoneE164("+919000000000");
        entity.setAddressLine("Address");
        entity.setFullName("Employee User");
        entity.setFirstName("Employee");
        entity.setLastName("User");
        entity.setStatus("ACTIVE");
        entity.setAdminUser(false);
        entity.setMustChangePassword(false);
        entity.setCreatedAt(Instant.parse("2026-06-15T07:00:00Z"));
        return entity;
    }

    private UserPropertyAccessEntity mapping(String userId, String propertyId) {
        UserPropertyAccessEntity entity = new UserPropertyAccessEntity();
        entity.setMappingId("upa-" + userId + "-" + propertyId);
        entity.setUserId(userId);
        entity.setTenantId("bikini-bottom");
        entity.setPropertyId(propertyId);
        return entity;
    }
}
