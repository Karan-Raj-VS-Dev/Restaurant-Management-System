package com.restaurant.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.restaurant.auth.api.LoginRequest;
import com.restaurant.auth.api.PasswordChangeRequest;
import com.restaurant.auth.api.PasswordResetConfirmRequest;
import com.restaurant.auth.api.PasswordResetRequest;
import com.restaurant.auth.config.AuthProperties;
import com.restaurant.auth.persistence.entity.AppUserEntity;
import com.restaurant.auth.persistence.entity.PasswordResetOtpEntity;
import com.restaurant.auth.persistence.entity.UserPropertyAccessEntity;
import com.restaurant.auth.persistence.repository.AppUserRepository;
import com.restaurant.auth.persistence.repository.PasswordResetOtpRepository;
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
class AuthServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordResetOtpRepository passwordResetOtpRepository;

    @Mock
    private UserPropertyAccessRepository userPropertyAccessRepository;

    @Mock
    private AuthTokenService authTokenService;

    private AuthProperties authProperties;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authProperties = new AuthProperties();
        authProperties.setOtpExpiryMinutes(10);
        authProperties.setExposeDevOtp(true);
        authProperties.setSessionTokenMinutes(480);
        authService = new AuthService(
                appUserRepository,
                passwordResetOtpRepository,
                userPropertyAccessRepository,
                authProperties,
                authTokenService
        );
    }

    @Test
    void loginReturnsAuthenticatedSessionForActiveUser() {
        AppUserEntity user = activeUser();
        when(appUserRepository.findByUsernameIgnoreCase("KaranRaj")).thenReturn(Optional.of(user));
        when(authTokenService.issueToken(user)).thenReturn("jwt-token");
        when(userPropertyAccessRepository.findByUserIdOrderByCreatedAtAsc("usr-001"))
                .thenReturn(List.of(mapping("krusty-krab")));

        AuthService.AuthenticatedLogin login = authService.login(new LoginRequest("KaranRaj", "Karan@Raj"));

        assertThat(login.token()).isEqualTo("jwt-token");
        assertThat(login.session().username()).isEqualTo("KaranRaj");
        assertThat(login.session().mappedPropertyIds()).containsExactly("krusty-krab");
        verify(appUserRepository).save(user);
        verify(authTokenService).issueToken(user);
    }

    @Test
    void loginRejectsWrongPassword() {
        AppUserEntity user = activeUser();
        when(appUserRepository.findByUsernameIgnoreCase("KaranRaj")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("KaranRaj", "wrong")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        verify(appUserRepository, never()).save(any());
        verify(authTokenService, never()).issueToken(any());
    }

    @Test
    void currentSessionLoadsMappedPropertiesAndUpdatesLastLogin() {
        AppUserEntity user = activeUser();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("usr-001")
                .claim("preferred_username", "KaranRaj")
                .build();
        when(appUserRepository.findById("usr-001")).thenReturn(Optional.of(user));
        when(userPropertyAccessRepository.findByUserIdOrderByCreatedAtAsc("usr-001"))
                .thenReturn(List.of(mapping("krusty-krab"), mapping("krusty-krab")));

        var session = authService.currentSession(jwt);

        assertThat(session.username()).isEqualTo("KaranRaj");
        assertThat(session.mappedPropertyIds()).containsExactly("krusty-krab");
        verify(appUserRepository).save(user);
    }

    @Test
    void requestPasswordResetReturnsHiddenResponseWhenUserMissing() {
        when(appUserRepository.findByEmailIgnoreCase("nobody@restaurant.local")).thenReturn(Optional.empty());

        var response = authService.requestPasswordReset(new PasswordResetRequest("nobody@restaurant.local"));

        assertThat(response.accountFound()).isFalse();
        verify(passwordResetOtpRepository, never()).save(any());
    }

    @Test
    void requestPasswordResetGeneratesAndSavesOtpForPhoneLookup() {
        AppUserEntity user = activeUser();
        user.setPhoneE164("+918901913123");
        when(appUserRepository.findByPhoneE164("+8901913123")).thenReturn(Optional.of(user));

        var response = authService.requestPasswordReset(new PasswordResetRequest("8901913123"));

        assertThat(response.accountFound()).isTrue();
        assertThat(response.deliveryChannel()).isEqualTo("PHONE");
        assertThat(response.devOtp()).isNotBlank();

        ArgumentCaptor<PasswordResetOtpEntity> captor = ArgumentCaptor.forClass(PasswordResetOtpEntity.class);
        verify(passwordResetOtpRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo("usr-001");
        assertThat(captor.getValue().getIdentifier()).isEqualTo("+918901913123");
    }

    @Test
    void confirmPasswordResetUpdatesPasswordAndMarksOtpUsed() {
        AppUserEntity user = activeUser();
        PasswordResetOtpEntity otp = new PasswordResetOtpEntity();
        otp.setOtpId("otp-001");
        otp.setUserId("usr-001");
        otp.setOtpCode("123456");
        otp.setExpiresAt(Instant.now().plusSeconds(300));
        when(appUserRepository.findByEmailIgnoreCase("karan@restaurant.local")).thenReturn(Optional.of(user));
        when(passwordResetOtpRepository.findTopByUserIdAndOtpCodeAndUsedAtIsNullOrderByCreatedAtDesc("usr-001", "123456"))
                .thenReturn(Optional.of(otp));

        var response = authService.confirmPasswordReset(
                new PasswordResetConfirmRequest("karan@restaurant.local", "123456", "New@Password1")
        );

        assertThat(response.username()).isEqualTo("KaranRaj");
        assertThat(user.getPassword()).isEqualTo("New@Password1");
        assertThat(user.isMustChangePassword()).isFalse();
        assertThat(otp.getUsedAt()).isNotNull();
        verify(appUserRepository).save(user);
        verify(passwordResetOtpRepository).save(otp);
    }

    @Test
    void changePasswordRejectsWrongCurrentPassword() {
        AppUserEntity user = activeUser();
        when(appUserRepository.findByUsernameIgnoreCase("KaranRaj")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.changePassword(
                new PasswordChangeRequest("KaranRaj", "Wrong@Password", "New@Password1")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(appUserRepository, never()).save(any());
    }

    private AppUserEntity activeUser() {
        AppUserEntity user = new AppUserEntity();
        user.setUserId("usr-001");
        user.setTenantId("bikini-bottom");
        user.setPropertyId("krusty-krab");
        user.setUsername("KaranRaj");
        user.setEmail("karan@restaurant.local");
        user.setPassword("Karan@Raj");
        user.setFullName("Karan Raj");
        user.setFirstName("Karan");
        user.setLastName("Raj");
        user.setPhoneCountryCode("+91");
        user.setPhoneNumber("8901913123");
        user.setPhoneE164("+918901913123");
        user.setStatus("ACTIVE");
        user.setAdminUser(false);
        user.setMustChangePassword(true);
        return user;
    }

    private UserPropertyAccessEntity mapping(String propertyId) {
        UserPropertyAccessEntity entity = new UserPropertyAccessEntity();
        entity.setMappingId("upa-" + propertyId);
        entity.setUserId("usr-001");
        entity.setTenantId("bikini-bottom");
        entity.setPropertyId(propertyId);
        return entity;
    }
}
