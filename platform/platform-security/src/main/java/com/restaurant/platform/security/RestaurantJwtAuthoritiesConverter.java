package com.restaurant.platform.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class RestaurantJwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {
        Set<String> authorities = new LinkedHashSet<>();
        authorities.add("ROLE_AUTHENTICATED_USER");
        extractSimpleRoles(source, authorities);
        extractRealmRoles(source, authorities);
        extractScopeAuthorities(source, authorities);
        return authorities.stream()
                .filter(Objects::nonNull)
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }

    private void extractSimpleRoles(Jwt jwt, Set<String> authorities) {
        Object rolesClaim = jwt.getClaims().get("roles");
        if (rolesClaim instanceof Collection<?> roleCollection) {
            for (Object role : roleCollection) {
                if (role instanceof String roleValue && !roleValue.isBlank()) {
                    authorities.add("ROLE_" + roleValue.trim().toUpperCase().replace('-', '_'));
                }
            }
            return;
        }

        if (rolesClaim instanceof String roleText && !roleText.isBlank()) {
            for (String role : roleText.split("\\s+")) {
                if (!role.isBlank()) {
                    authorities.add("ROLE_" + role.trim().toUpperCase().replace('-', '_'));
                }
            }
        }
    }

    private void extractRealmRoles(Jwt jwt, Set<String> authorities) {
        Object realmAccess = jwt.getClaims().get("realm_access");
        if (!(realmAccess instanceof Map<?, ?> realmAccessMap)) {
            return;
        }

        Object roles = realmAccessMap.get("roles");
        if (!(roles instanceof Collection<?> roleCollection)) {
            return;
        }

        for (Object role : roleCollection) {
            if (role instanceof String roleValue && !roleValue.isBlank()) {
                authorities.add("ROLE_" + roleValue.trim().toUpperCase().replace('-', '_'));
            }
        }
    }

    private void extractScopeAuthorities(Jwt jwt, Set<String> authorities) {
        Object scopeClaim = jwt.getClaims().get("scope");
        if (scopeClaim instanceof String scopeText && !scopeText.isBlank()) {
            for (String scope : scopeText.split("\\s+")) {
                if (!scope.isBlank()) {
                    authorities.add("SCOPE_" + scope.trim());
                }
            }
        }

        Object scopesClaim = jwt.getClaims().get("scp");
        if (scopesClaim instanceof Collection<?> scopeCollection) {
            for (Object scope : scopeCollection) {
                if (scope instanceof String scopeValue && !scopeValue.isBlank()) {
                    authorities.add("SCOPE_" + scopeValue.trim());
                }
            }
        }
    }
}
