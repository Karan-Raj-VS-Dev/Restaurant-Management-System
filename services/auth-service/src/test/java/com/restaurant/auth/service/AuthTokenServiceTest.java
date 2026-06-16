package com.restaurant.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.restaurant.auth.config.AuthProperties;
import com.restaurant.auth.persistence.entity.AppUserEntity;
import com.restaurant.auth.persistence.entity.UserPropertyAccessEntity;
import com.restaurant.auth.persistence.repository.UserPropertyAccessRepository;
import com.restaurant.platform.security.PlatformSecurityProperties;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

@ExtendWith(MockitoExtension.class)
class AuthTokenServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private UserPropertyAccessRepository userPropertyAccessRepository;

    private AuthTokenService authTokenService;

    @BeforeEach
    void setUp() {
        PlatformSecurityProperties securityProperties = new PlatformSecurityProperties();
        securityProperties.setJwtIssuer("restaurant-management-system");
        AuthProperties authProperties = new AuthProperties();
        authProperties.setSessionTokenMinutes(120);
        authTokenService = new AuthTokenService(jwtEncoder, securityProperties, authProperties, userPropertyAccessRepository);
    }

    @Test
    void issueTokenIncludesMappedPropertyIdsAndRoleClaims() {
        AppUserEntity user = new AppUserEntity();
        user.setUserId("usr-001");
        user.setUsername("KaranRaj");
        user.setFullName("Karan Raj");
        user.setTenantId("bikini-bottom");
        user.setPropertyId("krusty-krab");
        user.setAdminUser(false);
        when(userPropertyAccessRepository.findByUserIdOrderByCreatedAtAsc("usr-001"))
                .thenReturn(List.of(mapping("krusty-krab")));
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(
                Jwt.withTokenValue("signed-token")
                        .header("alg", "HS256")
                        .claim("sub", "usr-001")
                        .build()
        );

        String token = authTokenService.issueToken(user);

        assertThat(token).isEqualTo("signed-token");
        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());
        JwtClaimsSet claims = captor.getValue().getClaims();
        assertThat(claims.getSubject()).isEqualTo("usr-001");
        assertThat(claimList(claims, "property_ids")).containsExactly("krusty-krab");
        assertThat(claimList(claims, "roles")).containsExactly("restaurant-user");
    }

    @SuppressWarnings("unchecked")
    private List<String> claimList(JwtClaimsSet claims, String claimName) {
        return (List<String>) claims.getClaim(claimName);
    }

    private UserPropertyAccessEntity mapping(String propertyId) {
        UserPropertyAccessEntity entity = new UserPropertyAccessEntity();
        entity.setMappingId("upa-001");
        entity.setUserId("usr-001");
        entity.setTenantId("bikini-bottom");
        entity.setPropertyId(propertyId);
        return entity;
    }
}
