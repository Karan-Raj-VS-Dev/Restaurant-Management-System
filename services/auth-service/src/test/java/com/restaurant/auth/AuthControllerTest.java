package com.restaurant.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.restaurant.auth.api.LoginRequest;
import com.restaurant.auth.api.LoginResponse;
import com.restaurant.auth.api.PasswordChangeRequest;
import com.restaurant.auth.api.PasswordResetConfirmRequest;
import com.restaurant.auth.api.PasswordResetRequest;
import com.restaurant.auth.api.PasswordResetRequestedResponse;
import com.restaurant.auth.api.PasswordUpdateResponse;
import com.restaurant.auth.service.AuthCookieService;
import com.restaurant.auth.service.AuthService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private AuthCookieService authCookieService;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(authService, authCookieService);
    }

    @Test
    void sessionReturnsCurrentAuthenticatedSession() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user-001")
                .claim("preferred_username", "KaranRaj")
                .build();
        LoginResponse expected = loginResponse();
        when(authService.currentSession(jwt)).thenReturn(expected);

        LoginResponse response = controller.session(jwt);

        assertThat(response).isEqualTo(expected);
        verify(authService).currentSession(jwt);
    }

    @Test
    void loginReturnsSessionAndAddsCookieHeader() {
        LoginRequest request = new LoginRequest("KaranRaj", "Karan@Raj");
        LoginResponse session = loginResponse();
        AuthService.AuthenticatedLogin authenticatedLogin = new AuthService.AuthenticatedLogin(session, "jwt-token");
        ResponseCookie cookie = ResponseCookie.from("RMS_AUTH_TOKEN", "jwt-token").path("/").httpOnly(true).build();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(authService.login(request)).thenReturn(authenticatedLogin);
        when(authCookieService.sessionCookie("jwt-token")).thenReturn(cookie);

        LoginResponse returned = controller.login(request, response);

        assertThat(returned).isEqualTo(session);
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).contains("RMS_AUTH_TOKEN=jwt-token");
        verify(authService).login(request);
        verify(authCookieService).sessionCookie("jwt-token");
    }

    @Test
    void logoutAddsClearingCookieHeader() {
        ResponseCookie clearCookie = ResponseCookie.from("RMS_AUTH_TOKEN", "").path("/").maxAge(0).build();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(authCookieService.clearCookie()).thenReturn(clearCookie);

        controller.logout(response);

        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).contains("Max-Age=0");
        verify(authCookieService).clearCookie();
    }

    @Test
    void passwordResetRequestDelegatesToService() {
        PasswordResetRequest request = new PasswordResetRequest("karan@example.com");
        PasswordResetRequestedResponse expected = new PasswordResetRequestedResponse(true, "OTP sent", "EMAIL", "k***@mail.com", "123456");
        when(authService.requestPasswordReset(request)).thenReturn(expected);

        PasswordResetRequestedResponse response = controller.requestPasswordReset(request);

        assertThat(response).isEqualTo(expected);
        verify(authService).requestPasswordReset(request);
    }

    @Test
    void passwordResetConfirmDelegatesToService() {
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest("karan@example.com", "123456", "New@Password1");
        PasswordUpdateResponse expected = new PasswordUpdateResponse("KaranRaj", "Password updated");
        when(authService.confirmPasswordReset(request)).thenReturn(expected);

        PasswordUpdateResponse response = controller.confirmPasswordReset(request);

        assertThat(response).isEqualTo(expected);
        verify(authService).confirmPasswordReset(request);
    }

    @Test
    void passwordChangeDelegatesToService() {
        PasswordChangeRequest request = new PasswordChangeRequest("KaranRaj", "Old@Password1", "New@Password1");
        PasswordUpdateResponse expected = new PasswordUpdateResponse("KaranRaj", "Password updated");
        when(authService.changePassword(request)).thenReturn(expected);

        PasswordUpdateResponse response = controller.changePassword(request);

        assertThat(response).isEqualTo(expected);
        verify(authService).changePassword(request);
    }

    private LoginResponse loginResponse() {
        return new LoginResponse(
                "user-001",
                "bikini-bottom",
                "krusty-krab",
                List.of("krusty-krab"),
                "KaranRaj",
                "Karan Raj",
                false,
                false,
                "property-selection"
        );
    }
}
