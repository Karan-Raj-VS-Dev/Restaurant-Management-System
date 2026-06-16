package com.restaurant.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.platform.security.PlatformSecurityProperties;
import org.junit.jupiter.api.Test;

class AuthCookieServiceTest {

    @Test
    void sessionCookieUsesConfiguredCookieName() {
        PlatformSecurityProperties properties = new PlatformSecurityProperties();
        properties.setCookieName("CUSTOM_AUTH");
        AuthCookieService service = new AuthCookieService(properties);

        var cookie = service.sessionCookie("token-value");

        assertThat(cookie.getName()).isEqualTo("CUSTOM_AUTH");
        assertThat(cookie.getValue()).isEqualTo("token-value");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSameSite()).isEqualTo("Lax");
    }

    @Test
    void clearCookieExpiresImmediately() {
        PlatformSecurityProperties properties = new PlatformSecurityProperties();
        AuthCookieService service = new AuthCookieService(properties);

        var cookie = service.clearCookie();

        assertThat(cookie.getName()).isEqualTo("RMS_AUTH_TOKEN");
        assertThat(cookie.getMaxAge().getSeconds()).isZero();
    }
}
