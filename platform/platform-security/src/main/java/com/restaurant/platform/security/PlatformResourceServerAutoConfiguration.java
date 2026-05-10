package com.restaurant.platform.security;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import jakarta.servlet.http.Cookie;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

@AutoConfiguration
@EnableConfigurationProperties(PlatformSecurityProperties.class)
@ConditionalOnProperty(prefix = "app.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PlatformResourceServerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    SecurityFilterChain platformSecurityFilterChain(HttpSecurity http, PlatformSecurityProperties properties) throws Exception {
        String[] permitAllPatterns = properties.getPermitAllPaths().toArray(String[]::new);
        BearerTokenResolver bearerTokenResolver = bearerTokenResolver(properties);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(permitAllPatterns).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .bearerTokenResolver(bearerTokenResolver)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter(properties))));

        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean
    JwtDecoder jwtDecoder(PlatformSecurityProperties properties) {
        if (StringUtils.hasText(properties.getJwtSecret())) {
            SecretKey secretKey = new SecretKeySpec(properties.getJwtSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey).build();
            if (StringUtils.hasText(properties.getJwtIssuer())) {
                OAuth2TokenValidator<Jwt> validator = JwtValidators.createDefaultWithIssuer(properties.getJwtIssuer());
                decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validator));
            }
            return decoder;
        }
        return JwtDecoders.fromIssuerLocation(properties.getIssuerUri());
    }

    @Bean
    @ConditionalOnMissingBean
    JwtAuthenticationConverter jwtAuthenticationConverter(PlatformSecurityProperties properties) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setPrincipalClaimName(properties.getPrincipalClaimName());
        converter.setJwtGrantedAuthoritiesConverter(new RestaurantJwtAuthoritiesConverter());
        return converter;
    }

    @Bean
    @ConditionalOnMissingBean
    BearerTokenResolver bearerTokenResolver(PlatformSecurityProperties properties) {
        DefaultBearerTokenResolver defaultResolver = new DefaultBearerTokenResolver();
        return request -> {
            String authorizationToken = defaultResolver.resolve(request);
            if (StringUtils.hasText(authorizationToken)) {
                return authorizationToken;
            }
            if (request.getCookies() == null) {
                return null;
            }
            for (Cookie cookie : request.getCookies()) {
                if (properties.getCookieName().equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                    return cookie.getValue();
                }
            }
            return null;
        };
    }
}
