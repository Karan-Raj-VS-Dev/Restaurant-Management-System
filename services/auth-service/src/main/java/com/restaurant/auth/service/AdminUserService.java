package com.restaurant.auth.service;

import com.restaurant.auth.api.ManagedUserRequest;
import com.restaurant.auth.api.ManagedUserResponse;
import com.restaurant.auth.api.ManagedUserUpdateRequest;
import com.restaurant.auth.api.ManagedUsersResponse;
import com.restaurant.auth.persistence.entity.AppUserEntity;
import com.restaurant.auth.persistence.entity.UserPropertyAccessEntity;
import com.restaurant.auth.persistence.repository.AppUserRepository;
import com.restaurant.auth.persistence.repository.UserPropertyAccessRepository;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class AdminUserService {

    private final AppUserRepository appUserRepository;
    private final UserPropertyAccessRepository userPropertyAccessRepository;

    public AdminUserService(AppUserRepository appUserRepository,
                            UserPropertyAccessRepository userPropertyAccessRepository) {
        this.appUserRepository = appUserRepository;
        this.userPropertyAccessRepository = userPropertyAccessRepository;
    }

    @Transactional(readOnly = true)
    public ManagedUsersResponse listUsers(Jwt actorJwt) {
        AppUserEntity admin = requireAdmin(actorJwt);
        List<ManagedUserResponse> users = appUserRepository.findAllByTenantIdOrderByCreatedAtDesc(admin.getTenantId())
                .stream()
                .map(this::toResponse)
                .toList();
        return new ManagedUsersResponse(users.size(), users);
    }

    public ManagedUserResponse createUser(Jwt actorJwt, ManagedUserRequest request) {
        AppUserEntity admin = requireAdmin(actorJwt);
        ensureUniqueValues(request.username(), request.email(), buildPhoneE164(request.phoneCountryCode(), request.phoneNumber()), null);
        List<String> normalizedPropertyIds = normalizeMappedPropertyIds(request.mappedPropertyIds());

        AppUserEntity entity = new AppUserEntity();
        entity.setUserId("usr-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        entity.setTenantId(admin.getTenantId());
        entity.setUsername(request.username().trim());
        entity.setFirstName(request.firstName().trim());
        entity.setLastName(request.lastName().trim());
        entity.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        entity.setPhoneCountryCode(request.phoneCountryCode().trim());
        entity.setPhoneNumber(request.phoneNumber().trim());
        entity.setPhoneE164(buildPhoneE164(request.phoneCountryCode(), request.phoneNumber()));
        entity.setAddressLine(request.addressLine().trim());
        entity.setLatitude(request.latitude());
        entity.setLongitude(request.longitude());
        entity.setPropertyId(null);
        entity.setPassword(request.temporaryPassword());
        entity.setStatus("ACTIVE");
        entity.setAdminUser(false);
        entity.setMustChangePassword(true);
        entity.setFullName((request.firstName() + " " + request.lastName()).trim());
        AppUserEntity saved = appUserRepository.save(entity);
        syncPropertyAccess(saved, normalizedPropertyIds);
        return toResponse(saved);
    }

    public ManagedUserResponse updateUser(Jwt actorJwt, String userId, ManagedUserUpdateRequest request) {
        AppUserEntity admin = requireAdmin(actorJwt);
        AppUserEntity entity = appUserRepository.findById(userId)
                .filter(user -> user.getTenantId().equals(admin.getTenantId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User was not found"));

        String username = request.username() == null ? entity.getUsername() : request.username().trim();
        String email = request.email() == null ? entity.getEmail() : request.email().trim().toLowerCase(Locale.ROOT);
        String phoneE164 = (request.phoneCountryCode() == null || request.phoneNumber() == null)
                ? entity.getPhoneE164()
                : buildPhoneE164(request.phoneCountryCode(), request.phoneNumber());
        ensureUniqueValues(username, email, phoneE164, entity.getUserId());

        if (request.firstName() != null) {
            entity.setFirstName(request.firstName().trim());
        }
        if (request.lastName() != null) {
            entity.setLastName(request.lastName().trim());
        }
        entity.setUsername(username);
        entity.setEmail(email);
        if (request.phoneCountryCode() != null) {
            entity.setPhoneCountryCode(request.phoneCountryCode().trim());
        }
        if (request.phoneNumber() != null) {
            entity.setPhoneNumber(request.phoneNumber().trim());
        }
        entity.setPhoneE164(phoneE164);
        if (request.addressLine() != null) {
            entity.setAddressLine(request.addressLine().trim());
        }
        if (request.latitude() != null) {
            entity.setLatitude(request.latitude());
        }
        if (request.longitude() != null) {
            entity.setLongitude(request.longitude());
        }
        if (request.status() != null && !request.status().isBlank()) {
            entity.setStatus(request.status().trim().toUpperCase(Locale.ROOT));
        }
        String temporaryPassword = null;
        if (request.temporaryPassword() != null && !request.temporaryPassword().isBlank()) {
            temporaryPassword = request.temporaryPassword();
            entity.setPassword(temporaryPassword);
            entity.setMustChangePassword(true);
        }

        AppUserEntity saved = appUserRepository.save(entity);
        if (request.mappedPropertyIds() != null) {
            List<String> normalizedPropertyIds = normalizeMappedPropertyIds(request.mappedPropertyIds());
            syncPropertyAccess(saved, normalizedPropertyIds);
        }
        return toResponse(saved);
    }

    public void deleteUser(Jwt actorJwt, String userId) {
        AppUserEntity admin = requireAdmin(actorJwt);
        AppUserEntity entity = appUserRepository.findById(userId)
                .filter(user -> user.getTenantId().equals(admin.getTenantId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User was not found"));

        if (entity.isAdminUser() || entity.getUsername().equalsIgnoreCase(resolveActorUsername(actorJwt))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This user cannot be deleted from the admin console");
        }

        userPropertyAccessRepository.deleteByUserId(entity.getUserId());
        appUserRepository.delete(entity);
    }

    private AppUserEntity requireAdmin(Jwt actorJwt) {
        AppUserEntity admin = appUserRepository.findByUsernameIgnoreCase(resolveActorUsername(actorJwt))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin session is missing"));
        if (!admin.isAdminUser()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin users can manage tenant users");
        }
        return admin;
    }

    private String resolveActorUsername(Jwt actorJwt) {
        String username = actorJwt == null ? null : actorJwt.getClaimAsString("preferred_username");
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated token is missing a username");
        }
        return username.trim();
    }

    private void ensureUniqueValues(String username, String email, String phoneE164, String currentUserId) {
        appUserRepository.findByUsernameIgnoreCase(username).ifPresent(existing -> {
            if (!existing.getUserId().equals(currentUserId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already in use");
            }
        });
        appUserRepository.findByEmailIgnoreCase(email).ifPresent(existing -> {
            if (!existing.getUserId().equals(currentUserId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
            }
        });
        appUserRepository.findByPhoneE164(phoneE164).ifPresent(existing -> {
            if (!existing.getUserId().equals(currentUserId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number is already in use");
            }
        });
    }

    private String buildPhoneE164(String phoneCountryCode, String phoneNumber) {
        String cleanedCode = phoneCountryCode.trim().replaceAll("\\s+", "");
        String cleanedPhone = phoneNumber.trim().replaceAll("\\s+", "");
        return cleanedCode + cleanedPhone;
    }

    private ManagedUserResponse toResponse(AppUserEntity entity) {
        List<String> mappedPropertyIds = userPropertyAccessRepository.findByUserIdOrderByCreatedAtAsc(entity.getUserId())
                .stream()
                .map(UserPropertyAccessEntity::getPropertyId)
                .distinct()
                .toList();

        if (mappedPropertyIds.isEmpty() && entity.getPropertyId() != null && !entity.getPropertyId().isBlank()) {
            mappedPropertyIds = List.of(entity.getPropertyId());
        }

        return new ManagedUserResponse(
                entity.getUserId(),
                entity.getTenantId(),
                null,
                mappedPropertyIds,
                entity.getFirstName(),
                entity.getLastName(),
                entity.getFullName(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getPhoneCountryCode(),
                entity.getPhoneNumber(),
                entity.getAddressLine(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getStatus(),
                entity.isAdminUser(),
                entity.isMustChangePassword(),
                entity.getLastLoginAt(),
                entity.getCreatedAt()
        );
    }

    private void syncPropertyAccess(AppUserEntity user, List<String> normalizedPropertyIds) {
        user.setPropertyId(null);
        appUserRepository.save(user);

        userPropertyAccessRepository.deleteByUserId(user.getUserId());
        userPropertyAccessRepository.flush();
        for (String propertyId : normalizedPropertyIds) {
            UserPropertyAccessEntity mapping = new UserPropertyAccessEntity();
            mapping.setMappingId("upa-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
            mapping.setUserId(user.getUserId());
            mapping.setTenantId(user.getTenantId());
            mapping.setPropertyId(propertyId);
            userPropertyAccessRepository.saveAndFlush(mapping);
        }
    }

    private List<String> normalizeMappedPropertyIds(List<String> requestedMappedPropertyIds) {
        if (requestedMappedPropertyIds == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one property must be mapped");
        }
        List<String> normalizedPropertyIds = requestedMappedPropertyIds.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
        if (normalizedPropertyIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one property must be mapped");
        }
        return normalizedPropertyIds;
    }
}
