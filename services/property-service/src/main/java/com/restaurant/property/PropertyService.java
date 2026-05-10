package com.restaurant.property;

import com.restaurant.property.persistence.entity.PropertyEntity;
import com.restaurant.property.persistence.repository.PropertyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    public List<PropertyResponse> listProperties(String tenantId) {
        return propertyRepository.findByTenantIdOrderByPropertyNameAsc(tenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    public PropertyResponse getProperty(String tenantId, String propertyId) {
        return propertyRepository.findByTenantIdAndPropertyId(tenantId, propertyId)
                .map(this::toResponse)
                .orElseGet(() -> new PropertyResponse(tenantId, "chefy", propertyId, "Unknown Property", "Unknown", null, "Unknown", null, null, null, "UNKNOWN"));
    }

    public PropertyResponse createProperty(String tenantId, CreatePropertyRequest request) {
        if (propertyRepository.existsByTenantIdAndPropertyNameIgnoreCase(tenantId, request.name().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Property name is already in use for this tenant");
        }

        PropertyEntity entity = new PropertyEntity();
        entity.setPropertyId(buildPropertyId(request.name()));
        entity.setTenantId(tenantId);
        entity.setPropertyCode(buildPropertyCode(tenantId, request.name()));
        entity.setPropertyName(request.name().trim());
        entity.setAddressLine(request.addressLine().trim());
        entity.setCity(request.city().trim());
        entity.setState(blankToNull(request.state()));
        entity.setCountry(blankToNull(request.country()) == null ? "India" : request.country().trim());
        entity.setTimezone(blankToNull(request.timezone()) == null ? "Asia/Kolkata" : request.timezone().trim());
        entity.setLatitude(request.latitude());
        entity.setLongitude(request.longitude());
        entity.setStatus("ACTIVE");
        return toResponse(propertyRepository.save(entity));
    }

    public PropertyResponse updateProperty(String tenantId, String propertyId, UpdatePropertyRequest request) {
        PropertyEntity entity = propertyRepository.findByTenantIdAndPropertyId(tenantId, propertyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property was not found"));

        if (request.name() != null && !request.name().isBlank()) {
            entity.setPropertyName(request.name().trim());
        }
        if (request.addressLine() != null) {
            entity.setAddressLine(request.addressLine().trim());
        }
        if (request.city() != null && !request.city().isBlank()) {
            entity.setCity(request.city().trim());
        }
        if (request.state() != null) {
            entity.setState(blankToNull(request.state()));
        }
        if (request.country() != null && !request.country().isBlank()) {
            entity.setCountry(request.country().trim());
        }
        if (request.timezone() != null && !request.timezone().isBlank()) {
            entity.setTimezone(request.timezone().trim());
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
        return toResponse(propertyRepository.save(entity));
    }

    public void deleteProperty(String tenantId, String propertyId) {
        PropertyEntity entity = propertyRepository.findByTenantIdAndPropertyId(tenantId, propertyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property was not found"));
        propertyRepository.delete(entity);
    }

    private PropertyResponse toResponse(PropertyEntity property) {
        return new PropertyResponse(
                property.getTenantId(),
                "chefy",
                property.getPropertyId(),
                property.getPropertyName(),
                property.getCity(),
                property.getState(),
                property.getCountry(),
                property.getAddressLine(),
                property.getLatitude(),
                property.getLongitude(),
                property.getStatus()
        );
    }

    private String buildPropertyId(String propertyName) {
        String slug = propertyName.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (slug.isBlank()) {
            slug = "property";
        }
        return slug + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }

    private String buildPropertyCode(String tenantId, String propertyName) {
        String tenantCode = tenantId.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        String propertyCode = propertyName.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        return tenantCode + "-" + propertyCode + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
