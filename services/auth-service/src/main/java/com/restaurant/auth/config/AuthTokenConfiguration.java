package com.restaurant.auth.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.restaurant.platform.security.PlatformSecurityProperties;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class AuthTokenConfiguration {

    @Bean
    JwtEncoder jwtEncoder(PlatformSecurityProperties securityProperties) {
        SecretKey secretKey = new SecretKeySpec(securityProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }
}
