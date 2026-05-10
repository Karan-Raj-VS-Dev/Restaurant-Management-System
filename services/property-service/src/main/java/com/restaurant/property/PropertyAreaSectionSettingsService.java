package com.restaurant.property;

import com.restaurant.property.persistence.entity.PropertyAreaSectionEntity;
import com.restaurant.property.persistence.repository.PropertyAreaSectionRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class PropertyAreaSectionSettingsService {

    private final PropertyAreaSectionRepository propertyAreaSectionRepository;

    public PropertyAreaSectionSettingsService(PropertyAreaSectionRepository propertyAreaSectionRepository) {
        this.propertyAreaSectionRepository = propertyAreaSectionRepository;
    }

    @PostConstruct
    void seedDefaults() {
        seed(
                "bikini-bottom",
                "krusty-krab",
                "main-floor__dining",
                "Main floor",
                "Dining",
                12,
                List.of("Neha", "Anu"),
                List.of("Suresh"),
                "ACTIVE"
        );
        seed(
                "bikini-bottom",
                "krusty-krab",
                "main-floor__patio",
                "Main floor",
                "Patio",
                6,
                List.of("Karthi"),
                List.of("Suresh"),
                "ACTIVE"
        );
    }

    public AreaSectionSettingsSummaryResponse getSummary(String tenantId, String propertyId) {
        return new AreaSectionSettingsSummaryResponse(
                List.of("floor name", "section name", "table capacity", "waiters", "cleaners", "status"),
                propertyAreaSectionRepository.findByTenantIdAndPropertyIdOrderByFloorNameAscSectionNameAsc(tenantId, propertyId).stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    public AreaSectionSettingResponse create(String tenantId, String propertyId, UpsertAreaSectionSettingRequest request) {
        String areaSectionId = normalize(request.floorName()) + "__" + normalize(request.sectionName());
        return save(tenantId, propertyId, areaSectionId, request);
    }

    public AreaSectionSettingResponse update(String tenantId, String propertyId, String areaSectionId, UpsertAreaSectionSettingRequest request) {
        return save(tenantId, propertyId, areaSectionId, request);
    }

    private AreaSectionSettingResponse save(String tenantId,
                                            String propertyId,
                                            String areaSectionId,
                                            UpsertAreaSectionSettingRequest request) {
        PropertyAreaSectionEntity entity = propertyAreaSectionRepository
                .findByTenantIdAndPropertyIdAndAreaSectionId(tenantId, propertyId, areaSectionId)
                .orElseGet(PropertyAreaSectionEntity::new);
        entity.setAreaSectionId(areaSectionId);
        entity.setTenantId(tenantId);
        entity.setPropertyId(propertyId);
        entity.setFloorName(request.floorName());
        entity.setSectionName(request.sectionName());
        entity.setMaxTableCount(request.maxTableCount());
        entity.setWaiterNames(joinNames(request.waiterNames()));
        entity.setCleanerNames(joinNames(request.cleanerNames()));
        entity.setStatus(request.status());
        return toResponse(propertyAreaSectionRepository.save(entity));
    }

    private AreaSectionSettingResponse toResponse(PropertyAreaSectionEntity entity) {
        return new AreaSectionSettingResponse(
                entity.getAreaSectionId(),
                entity.getFloorName(),
                entity.getSectionName(),
                entity.getMaxTableCount(),
                splitNames(entity.getWaiterNames()),
                splitNames(entity.getCleanerNames()),
                entity.getStatus()
        );
    }

    private void seed(String tenantId,
                      String propertyId,
                      String areaSectionId,
                      String floorName,
                      String sectionName,
                      int maxTableCount,
                      List<String> waiterNames,
                      List<String> cleanerNames,
                      String status) {
        if (propertyAreaSectionRepository.existsByTenantIdAndPropertyIdAndAreaSectionId(tenantId, propertyId, areaSectionId)) {
            return;
        }
        PropertyAreaSectionEntity entity = new PropertyAreaSectionEntity();
        entity.setAreaSectionId(areaSectionId);
        entity.setTenantId(tenantId);
        entity.setPropertyId(propertyId);
        entity.setFloorName(floorName);
        entity.setSectionName(sectionName);
        entity.setMaxTableCount(maxTableCount);
        entity.setWaiterNames(joinNames(waiterNames));
        entity.setCleanerNames(joinNames(cleanerNames));
        entity.setStatus(status);
        propertyAreaSectionRepository.save(entity);
    }

    private List<String> splitNames(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Stream.of(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }

    private String joinNames(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return values.stream()
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .distinct()
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }
}
