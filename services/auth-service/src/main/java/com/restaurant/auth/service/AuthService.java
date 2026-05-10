package com.restaurant.auth.service;

import com.restaurant.auth.api.LoginRequest;
import com.restaurant.auth.api.LoginResponse;
import com.restaurant.auth.api.PasswordChangeRequest;
import com.restaurant.auth.api.PasswordResetConfirmRequest;
import com.restaurant.auth.api.PasswordResetRequest;
import com.restaurant.auth.api.PasswordResetRequestedResponse;
import com.restaurant.auth.api.PasswordUpdateResponse;
import com.restaurant.auth.config.AuthProperties;
import com.restaurant.auth.persistence.entity.AppUserEntity;
import com.restaurant.auth.persistence.entity.PasswordResetOtpEntity;
import com.restaurant.auth.persistence.entity.UserPropertyAccessEntity;
import com.restaurant.auth.persistence.repository.AppUserRepository;
import com.restaurant.auth.persistence.repository.PasswordResetOtpRepository;
import com.restaurant.auth.persistence.repository.UserPropertyAccessRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    private final AppUserRepository appUserRepository;
    private final PasswordResetOtpRepository passwordResetOtpRepository;
    private final UserPropertyAccessRepository userPropertyAccessRepository;
    private final AuthProperties authProperties;
    private final AuthTokenService authTokenService;

    public AuthService(
            AppUserRepository appUserRepository,
            PasswordResetOtpRepository passwordResetOtpRepository,
            UserPropertyAccessRepository userPropertyAccessRepository,
            AuthProperties authProperties,
            AuthTokenService authTokenService
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordResetOtpRepository = passwordResetOtpRepository;
        this.userPropertyAccessRepository = userPropertyAccessRepository;
        this.authProperties = authProperties;
        this.authTokenService = authTokenService;
    }

    public AuthenticatedLogin login(LoginRequest request) {
        AppUserEntity user = appUserRepository.findByUsernameIgnoreCase(request.username().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        if (user.getPassword() == null || !"ACTIVE".equalsIgnoreCase(user.getStatus()) || !request.password().equals(user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        user.setLastLoginAt(Instant.now());
        appUserRepository.save(user);
        return new AuthenticatedLogin(toLoginResponse(user), authTokenService.issueToken(user));
    }

    public LoginResponse currentSession(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        String subject = jwt.getSubject();
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated token is missing a username");
        }

        AppUserEntity user = findUserByToken(subject, username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Authenticated user is not mapped inside the platform"));

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authenticated user is inactive inside the platform");
        }
        if (subject != null && !subject.isBlank() && !subject.equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authenticated token does not match the mapped platform user");
        }
        user.setLastLoginAt(Instant.now());
        appUserRepository.save(user);
        return toLoginResponse(user);
    }

    private java.util.Optional<AppUserEntity> findUserByToken(String subject, String username) {
        if (subject == null || subject.isBlank()) {
            return appUserRepository.findByUsernameIgnoreCase(username.trim());
        }
        return appUserRepository.findById(subject)
                .or(() -> appUserRepository.findByUsernameIgnoreCase(username.trim()));
    }

    public PasswordResetRequestedResponse requestPasswordReset(PasswordResetRequest request) {
        AppUserEntity user = findByIdentifier(request.identifier());
        if (user == null) {
            return new PasswordResetRequestedResponse(
                    false,
                    "If an account exists, an OTP will be sent to the registered channel.",
                    null,
                    null,
                    null
            );
        }

        passwordResetOtpRepository.deleteByUserId(user.getUserId());

        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100_000, 999_999));
        PasswordResetOtpEntity entity = new PasswordResetOtpEntity();
        entity.setOtpId("otp-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        entity.setUserId(user.getUserId());
        entity.setIdentifier(resolveDeliveryIdentifier(user, request.identifier()));
        entity.setDeliveryChannel(resolveDeliveryChannel(request.identifier()));
        entity.setOtpCode(otp);
        entity.setExpiresAt(Instant.now().plus(authProperties.getOtpExpiryMinutes(), ChronoUnit.MINUTES));
        passwordResetOtpRepository.save(entity);

        LOGGER.info("Generated password reset OTP for {} via {}: {}", user.getUsername(), entity.getDeliveryChannel(), otp);

        return new PasswordResetRequestedResponse(
                true,
                "An OTP has been generated for the matched account.",
                entity.getDeliveryChannel(),
                maskDestination(entity.getIdentifier(), entity.getDeliveryChannel()),
                authProperties.isExposeDevOtp() ? otp : null
        );
    }

    public PasswordUpdateResponse confirmPasswordReset(PasswordResetConfirmRequest request) {
        AppUserEntity user = requireUserByIdentifier(request.identifier());
        PasswordResetOtpEntity otp = passwordResetOtpRepository
                .findTopByUserIdAndOtpCodeAndUsedAtIsNullOrderByCreatedAtDesc(user.getUserId(), request.otp().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP is invalid"));

        if (otp.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP has expired");
        }

        user.setPassword(request.newPassword());
        user.setMustChangePassword(false);
        appUserRepository.save(user);

        otp.setUsedAt(Instant.now());
        passwordResetOtpRepository.save(otp);

        return new PasswordUpdateResponse(user.getUsername(), "Password updated successfully. Please sign in again.");
    }

    public PasswordUpdateResponse changePassword(PasswordChangeRequest request) {
        AppUserEntity user = appUserRepository.findByUsernameIgnoreCase(request.username().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User was not found"));

        if (!request.currentPassword().equals(user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        user.setPassword(request.newPassword());
        user.setMustChangePassword(false);
        appUserRepository.save(user);
        return new PasswordUpdateResponse(user.getUsername(), "Password updated successfully.");
    }

    private AppUserEntity requireUserByIdentifier(String identifier) {
        AppUserEntity user = findByIdentifier(identifier);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account was not found for the provided email or phone");
        }
        return user;
    }

    private AppUserEntity findByIdentifier(String identifier) {
        String cleaned = identifier == null ? "" : identifier.trim();
        if (cleaned.isBlank()) {
            return null;
        }
        if (cleaned.contains("@")) {
            return appUserRepository.findByEmailIgnoreCase(cleaned).orElse(null);
        }
        return appUserRepository.findByPhoneE164(normalizePhone(cleaned)).orElse(null);
    }

    private String normalizePhone(String raw) {
        String cleaned = raw.replaceAll("[^\\d+]", "");
        if (cleaned.startsWith("00")) {
            return "+" + cleaned.substring(2);
        }
        return cleaned.startsWith("+") ? cleaned : "+" + cleaned;
    }

    private String resolveDeliveryChannel(String identifier) {
        return identifier != null && identifier.contains("@") ? "EMAIL" : "PHONE";
    }

    private String resolveDeliveryIdentifier(AppUserEntity user, String identifier) {
        if (identifier != null && identifier.contains("@")) {
            return user.getEmail();
        }
        return user.getPhoneE164();
    }

    private String maskDestination(String destination, String channel) {
        if (destination == null || destination.isBlank()) {
            return null;
        }
        if ("EMAIL".equals(channel)) {
            int atIndex = destination.indexOf('@');
            if (atIndex <= 1) {
                return destination;
            }
            return destination.substring(0, 1) + "***" + destination.substring(atIndex - 1);
        }
        if (destination.length() <= 4) {
            return destination;
        }
        return "***" + destination.substring(destination.length() - 4);
    }

    private LoginResponse toLoginResponse(AppUserEntity user) {
        java.util.List<String> mappedPropertyIds = userPropertyAccessRepository.findByUserIdOrderByCreatedAtAsc(user.getUserId())
                .stream()
                .map(UserPropertyAccessEntity::getPropertyId)
                .distinct()
                .toList();

        if (mappedPropertyIds.isEmpty() && user.getPropertyId() != null && !user.getPropertyId().isBlank()) {
            mappedPropertyIds = java.util.List.of(user.getPropertyId());
        }

        return new LoginResponse(
                user.getUserId(),
                user.getTenantId(),
                null,
                mappedPropertyIds,
                user.getUsername(),
                user.getFullName(),
                user.isAdminUser(),
                user.isMustChangePassword(),
                user.isAdminUser() ? "property-selection" : "property-selection"
        );
    }

    public record AuthenticatedLogin(LoginResponse session, String token) {
    }
}
