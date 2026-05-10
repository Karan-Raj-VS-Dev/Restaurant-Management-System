package com.restaurant.auth.service;

import com.restaurant.auth.config.AuthProperties;
import com.restaurant.auth.persistence.entity.AppUserEntity;
import com.restaurant.auth.persistence.entity.UserPropertyAccessEntity;
import com.restaurant.auth.persistence.repository.UserPropertyAccessRepository;
import com.restaurant.platform.security.PlatformSecurityProperties;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class AuthTokenService {

    private final JwtEncoder jwtEncoder;
    private final PlatformSecurityProperties securityProperties;
    private final AuthProperties authProperties;
    private final UserPropertyAccessRepository userPropertyAccessRepository;

    public AuthTokenService(
            JwtEncoder jwtEncoder,
            PlatformSecurityProperties securityProperties,
            AuthProperties authProperties,
            UserPropertyAccessRepository userPropertyAccessRepository
    ) {
        this.jwtEncoder = jwtEncoder;
        this.securityProperties = securityProperties;
        this.authProperties = authProperties;
        this.userPropertyAccessRepository = userPropertyAccessRepository;
    }

    public String issueToken(AppUserEntity user) {
        Instant issuedAt = Instant.now();
        List<String> mappedPropertyIds = userPropertyAccessRepository.findByUserIdOrderByCreatedAtAsc(user.getUserId())
                .stream()
                .map(UserPropertyAccessEntity::getPropertyId)
                .distinct()
                .toList();
        if (mappedPropertyIds.isEmpty() && user.getPropertyId() != null && !user.getPropertyId().isBlank()) {
            mappedPropertyIds = List.of(user.getPropertyId());
        }

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(securityProperties.getJwtIssuer())
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(authProperties.getSessionTokenMinutes(), ChronoUnit.MINUTES))
                .subject(user.getUserId())
                .claim("preferred_username", user.getUsername())
                .claim("full_name", user.getFullName())
                .claim("tenant_id", user.getTenantId())
                .claim("property_ids", mappedPropertyIds)
                .claim("admin_user", user.isAdminUser())
                .claim("roles", user.isAdminUser() ? List.of("platform-admin") : List.of("restaurant-user"))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
