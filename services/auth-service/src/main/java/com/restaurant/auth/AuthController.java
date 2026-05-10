package com.restaurant.auth;

import com.restaurant.auth.api.LoginRequest;
import com.restaurant.auth.api.LoginResponse;
import com.restaurant.auth.api.PasswordChangeRequest;
import com.restaurant.auth.api.PasswordResetConfirmRequest;
import com.restaurant.auth.api.PasswordResetRequest;
import com.restaurant.auth.api.PasswordResetRequestedResponse;
import com.restaurant.auth.api.PasswordUpdateResponse;
import com.restaurant.auth.service.AuthCookieService;
import com.restaurant.auth.service.AuthService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({
        "/api/auth",
        "/chefy/tenant/{tenantId}/api/auth",
        "/chefy/tenant/{tenantId}/property/{propertyId}/api/auth"
})
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService authCookieService;

    public AuthController(AuthService authService, AuthCookieService authCookieService) {
        this.authService = authService;
        this.authCookieService = authCookieService;
    }

    @GetMapping("/session")
    public LoginResponse session(@AuthenticationPrincipal Jwt jwt) {
        return authService.currentSession(jwt);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthService.AuthenticatedLogin login = authService.login(request);
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.sessionCookie(login.token()).toString());
        return login.session();
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.clearCookie().toString());
    }

    @PostMapping("/password-reset/request")
    public PasswordResetRequestedResponse requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        return authService.requestPasswordReset(request);
    }

    @PostMapping("/password-reset/confirm")
    public PasswordUpdateResponse confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        return authService.confirmPasswordReset(request);
    }

    @PostMapping("/password-change")
    public PasswordUpdateResponse changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        return authService.changePassword(request);
    }
}
