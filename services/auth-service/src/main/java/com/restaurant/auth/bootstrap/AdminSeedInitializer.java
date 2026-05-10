package com.restaurant.auth.bootstrap;

import com.restaurant.auth.config.AuthProperties;
import com.restaurant.auth.persistence.entity.AppUserEntity;
import com.restaurant.auth.persistence.entity.UserPropertyAccessEntity;
import com.restaurant.auth.persistence.repository.AppUserRepository;
import com.restaurant.auth.persistence.repository.UserPropertyAccessRepository;
import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class AdminSeedInitializer implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final UserPropertyAccessRepository userPropertyAccessRepository;
    private final AuthProperties authProperties;

    public AdminSeedInitializer(
            AppUserRepository appUserRepository,
            UserPropertyAccessRepository userPropertyAccessRepository,
            AuthProperties authProperties
    ) {
        this.appUserRepository = appUserRepository;
        this.userPropertyAccessRepository = userPropertyAccessRepository;
        this.authProperties = authProperties;
    }

    @Override
    public void run(String... args) {
        AppUserEntity entity = appUserRepository.findByUsernameIgnoreCase(authProperties.getSeedAdminUsername())
                .orElseGet(AppUserEntity::new);
        AppUserEntity saved = appUserRepository.save(applySeedAdmin(entity));
        userPropertyAccessRepository.deleteByUserId(saved.getUserId());
        userPropertyAccessRepository.flush();
        UserPropertyAccessEntity mapping = new UserPropertyAccessEntity();
        mapping.setMappingId("upa-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        mapping.setUserId(saved.getUserId());
        mapping.setTenantId(saved.getTenantId());
        mapping.setPropertyId(saved.getPropertyId());
        userPropertyAccessRepository.saveAndFlush(mapping);
    }

    private AppUserEntity applySeedAdmin(AppUserEntity entity) {
        if (entity.getUserId() == null || entity.getUserId().isBlank()) {
            entity.setUserId("usr-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        }
        entity.setTenantId(authProperties.getDefaultTenantId());
        entity.setPropertyId(authProperties.getDefaultPropertyId());
        entity.setUsername(authProperties.getSeedAdminUsername());
        entity.setFirstName("King");
        entity.setLastName("Chef");
        entity.setFullName("King Chef");
        entity.setEmail(authProperties.getSeedAdminEmail());
        entity.setPhoneCountryCode(authProperties.getSeedAdminPhoneCountryCode());
        entity.setPhoneNumber(authProperties.getSeedAdminPhoneNumber());
        entity.setPassword(authProperties.getSeedAdminPassword());
        entity.setStatus("ACTIVE");
        entity.setAdminUser(true);
        entity.setMustChangePassword(false);
        entity.setAddressLine("Restaurant HQ");
        return entity;
    }
}
