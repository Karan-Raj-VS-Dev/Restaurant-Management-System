package com.restaurant.auth.service;

import com.restaurant.platform.security.PlatformSecurityProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class AuthCookieService {

    private final PlatformSecurityProperties securityProperties;

    public AuthCookieService(PlatformSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    public ResponseCookie sessionCookie(String token) {
        return ResponseCookie.from(securityProperties.getCookieName(), token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .build();
    }

    public ResponseCookie clearCookie() {
        return ResponseCookie.from(securityProperties.getCookieName(), "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
    }
}
