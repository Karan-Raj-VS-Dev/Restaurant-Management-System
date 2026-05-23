package com.restaurant.table;

import com.restaurant.platform.eventing.AggregateTypes;
import com.restaurant.platform.eventing.DomainEventPublisher;
import com.restaurant.platform.eventing.EventEnvelopeFactory;
import com.restaurant.platform.eventing.EventKeys;
import com.restaurant.platform.eventing.contract.TableAssignedEvent;
import com.restaurant.platform.eventing.contract.TableStatusChangedEvent;
import com.restaurant.table.persistence.entity.RestaurantTableEntity;
import com.restaurant.table.persistence.repository.RestaurantTableRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TableService {

    private static final Duration RESERVATION_WARNING_WINDOW = Duration.ofMinutes(30);
    private static final Duration CLEANING_DELAY = Duration.ofSeconds(120);
    private static final Duration CLEANER_READY_DELAY = Duration.ofSeconds(300);

    private final Map<String, TableConfiguration> tables = new ConcurrentHashMap<>();
    private final RestaurantTableRepository restaurantTableRepository;
    private final EventEnvelopeFactory eventEnvelopeFactory;
    private final DomainEventPublisher domainEventPublisher;

    public TableService(RestaurantTableRepository restaurantTableRepository,
                        EventEnvelopeFactory eventEnvelopeFactory,
                        DomainEventPublisher domainEventPublisher) {
        this.restaurantTableRepository = restaurantTableRepository;
        this.eventEnvelopeFactory = eventEnvelopeFactory;
        this.domainEventPublisher = domainEventPublisher;
        seed();
    }

    public List<TableResponse> listTables(String tenantId, String propertyId) {
        return loadScopedConfigurations(tenantId, propertyId).stream()
                .map(configuration -> syncDueTransition(tenantId, propertyId, configuration))
                .filter(TableConfiguration::active)
                .map(this::toTableResponse)
                .toList();
    }

    public TableSettingsSummaryResponse getSettingsSummary(String tenantId, String propertyId) {
        List<TableSettingRecordResponse> scopedTables = loadScopedConfigurations(tenantId, propertyId).stream()
                .map(configuration -> syncDueTransition(tenantId, propertyId, configuration))
                .map(this::toTableSettingRecord)
                .toList();
        return new TableSettingsSummaryResponse(
                scopedTables.size(),
                List.of("table code", "display name", "capacity", "section", "status"),
                scopedTables
        );
    }

    public TableSettingRecordResponse createTableSetting(String tenantId, String propertyId, UpsertTableSettingRequest request) {
        String tableId = normalize(request.tableNumber());
        TableConfiguration configuration = new TableConfiguration(
                tableId,
                propertyId,
                request.tableNumber(),
                request.displayName(),
                request.floorName(),
                request.sectionName(),
                request.capacity(),
                parseStatus(request.status()),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                request.active()
        );
        tables.put(key(tenantId, propertyId, tableId), configuration);
        persistTableConfiguration(tenantId, configuration);
        return toTableSettingRecord(configuration);
    }

    public TableSettingRecordResponse updateTableSetting(String tenantId, String propertyId, String tableId, UpsertTableSettingRequest request) {
        String scopedKey = key(tenantId, propertyId, tableId);
        TableConfiguration current = tables.getOrDefault(scopedKey, new TableConfiguration(
                tableId,
                propertyId,
                request.tableNumber(),
                request.displayName(),
                request.floorName(),
                request.sectionName(),
                request.capacity(),
                parseStatus(request.status()),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                request.active()
        ));
        TableConfiguration updated = applySettingsUpdate(current, propertyId, request);
        tables.put(scopedKey, updated);
        persistTableConfiguration(tenantId, updated);
        return toTableSettingRecord(updated);
    }

    public TableResponse assignTable(String tenantId, String propertyId, AssignTableRequest request) {
        return updateStatus(
                tenantId,
                request.tableId(),
                propertyId,
                new UpdateTableStatusRequest(
                        TableStatus.OCCUPIED.name(),
                        request.capacity(),
                        request.waiterId(),
                        null,
                        null,
                        null,
                        true,
                        false
                )
        );
    }

    public TableResponse updateStatus(String tenantId, String tableId, String propertyId, UpdateTableStatusRequest request) {
        String scopedKey = key(tenantId, propertyId, tableId);
        TableConfiguration current = syncDueTransition(tenantId, propertyId, getConfiguration(tenantId, propertyId, tableId));

        TableStatus targetStatus = parseStatus(request.targetStatus());
        TableConfiguration updated = switch (targetStatus) {
            case OCCUPIED -> occupyTable(current, request);
            case RESERVED -> reserveTable(current, request);
            case NEEDS_CLEANING -> markNeedsCleaning(current, request);
            case AVAILABLE -> markAvailable(current, request);
            case UNAVAILABLE -> markUnavailable(current);
        };

        tables.put(scopedKey, updated);
        persistTableConfiguration(tenantId, updated);

        if (updated.status() == TableStatus.OCCUPIED) {
            publishAssignment(tenantId, toTableResponse(updated));
        }
        if (updated.status() != current.status()) {
            publishStatusChange(tenantId, toTableResponse(updated));
        }
        return toTableResponse(updated);
    }

    public TableResponse scheduleNeedsCleaningAfterPayment(String tenantId, String propertyId, String tableId) {
        return updateStatus(
                tenantId,
                tableId,
                propertyId,
                new UpdateTableStatusRequest(
                        TableStatus.NEEDS_CLEANING.name(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        false,
                        false
                )
        );
    }

    private void publishAssignment(String tenantId, TableResponse response) {
        domainEventPublisher.publish(eventEnvelopeFactory.create(
                EventKeys.TABLE_ASSIGNED,
                AggregateTypes.TABLE,
                response.tableId(),
                response.propertyId(),
                response.tableId(),
                null,
                new TableAssignedEvent(
                        response.tableId(),
                        tenantId,
                        response.propertyId(),
                        response.waiterId(),
                        response.currentPartySize() == null ? response.capacity() : response.currentPartySize(),
                        response.status().name(),
                        Instant.now()
                )
        ));
    }

    private void publishStatusChange(String tenantId, TableResponse response) {
        domainEventPublisher.publish(eventEnvelopeFactory.create(
                EventKeys.TABLE_STATUS_CHANGED,
                AggregateTypes.TABLE,
                response.tableId(),
                response.propertyId(),
                response.tableId(),
                null,
                new TableStatusChangedEvent(
                        response.tableId(),
                        tenantId,
                        response.propertyId(),
                        response.status().name(),
                        Instant.now()
                )
        ));
    }

    private TableConfiguration occupyTable(TableConfiguration current, UpdateTableStatusRequest request) {
        if (current.status() == TableStatus.UNAVAILABLE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This table is marked unavailable in property settings.");
        }
        if (current.status() == TableStatus.OCCUPIED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This table is already occupied.");
        }
        int partySize = Objects.requireNonNullElse(request.partySize(), 0);
        if (partySize <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter the number of guests before marking the table occupied.");
        }
        if (partySize > current.capacity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This table can only seat " + current.capacity() + " guests.");
        }
        if (request.waiterId() == null || request.waiterId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a server before starting the floor order.");
        }

        if (current.status() == TableStatus.RESERVED && current.reservationTime() != null) {
            Duration untilReservation = Duration.between(Instant.now(), current.reservationTime());
            boolean withinWarningWindow = !untilReservation.isNegative() && untilReservation.compareTo(RESERVATION_WARNING_WINDOW) <= 0;
            if (withinWarningWindow && !Boolean.TRUE.equals(request.overrideReservationWarning())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This table is reserved within the next 30 minutes. Override the reservation only if you already have another table ready for that booking."
                );
            }
        }

        return new TableConfiguration(
                current.tableId(),
                current.propertyId(),
                current.tableNumber(),
                current.displayName(),
                current.floorName(),
                current.sectionName(),
                current.capacity(),
                TableStatus.OCCUPIED,
                request.waiterId(),
                null,
                partySize,
                null,
                null,
                null,
                null,
                current.active()
        );
    }

    private TableConfiguration reserveTable(TableConfiguration current, UpdateTableStatusRequest request) {
        int partySize = request.reservationPartySize() != null
                ? request.reservationPartySize()
                : Objects.requireNonNullElse(request.partySize(), 0);
        if (partySize <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter the reservation party size before reserving the table.");
        }
        if (partySize > current.capacity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This table can only seat " + current.capacity() + " guests.");
        }
        if (request.reservationTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose the reservation time for this table.");
        }
        if (request.reservationTime().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation time must be in the future.");
        }

        return new TableConfiguration(
                current.tableId(),
                current.propertyId(),
                current.tableNumber(),
                current.displayName(),
                current.floorName(),
                current.sectionName(),
                current.capacity(),
                TableStatus.RESERVED,
                null,
                null,
                null,
                partySize,
                request.reservationTime(),
                null,
                null,
                current.active()
        );
    }

    private TableConfiguration markNeedsCleaning(TableConfiguration current, UpdateTableStatusRequest request) {
        if (!Boolean.TRUE.equals(request.immediate()) && current.pendingStatus() == TableStatus.NEEDS_CLEANING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This table is already queued to move into cleaning.");
        }
        if (Boolean.TRUE.equals(request.immediate())) {
            return new TableConfiguration(
                    current.tableId(),
                    current.propertyId(),
                    current.tableNumber(),
                    current.displayName(),
                    current.floorName(),
                    current.sectionName(),
                    current.capacity(),
                    TableStatus.NEEDS_CLEANING,
                    null,
                    current.cleanerId(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    current.active()
            );
        }

        return new TableConfiguration(
                current.tableId(),
                current.propertyId(),
                current.tableNumber(),
                current.displayName(),
                current.floorName(),
                current.sectionName(),
                current.capacity(),
                current.status(),
                current.waiterId(),
                current.cleanerId(),
                current.currentPartySize(),
                current.reservationPartySize(),
                current.reservationTime(),
                TableStatus.NEEDS_CLEANING,
                Instant.now().plus(CLEANING_DELAY),
                current.active()
        );
    }

    private TableConfiguration markAvailable(TableConfiguration current, UpdateTableStatusRequest request) {
        boolean cleanerRequired = current.status() == TableStatus.NEEDS_CLEANING || current.pendingStatus() == TableStatus.AVAILABLE;
        if (cleanerRequired && (request.cleanerId() == null || request.cleanerId().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a cleaner before returning the table to available.");
        }
        if (cleanerRequired && !Boolean.TRUE.equals(request.immediate()) && current.pendingStatus() == TableStatus.AVAILABLE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A cleaner cycle is already running for this table.");
        }

        if (cleanerRequired && !Boolean.TRUE.equals(request.immediate())) {
            return new TableConfiguration(
                    current.tableId(),
                    current.propertyId(),
                    current.tableNumber(),
                    current.displayName(),
                    current.floorName(),
                    current.sectionName(),
                    current.capacity(),
                    TableStatus.NEEDS_CLEANING,
                    null,
                    request.cleanerId(),
                    null,
                    null,
                    null,
                    TableStatus.AVAILABLE,
                    Instant.now().plus(CLEANER_READY_DELAY),
                    current.active()
            );
        }

        return clearRuntimeState(new TableConfiguration(
                current.tableId(),
                current.propertyId(),
                current.tableNumber(),
                current.displayName(),
                current.floorName(),
                current.sectionName(),
                current.capacity(),
                TableStatus.AVAILABLE,
                null,
                request.cleanerId(),
                null,
                null,
                null,
                null,
                null,
                current.active()
        ), TableStatus.AVAILABLE);
    }

    private TableConfiguration markUnavailable(TableConfiguration current) {
        return clearRuntimeState(new TableConfiguration(
                current.tableId(),
                current.propertyId(),
                current.tableNumber(),
                current.displayName(),
                current.floorName(),
                current.sectionName(),
                current.capacity(),
                TableStatus.UNAVAILABLE,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                current.active()
        ), TableStatus.UNAVAILABLE);
    }

    private TableConfiguration clearRuntimeState(TableConfiguration current, TableStatus status) {
        return new TableConfiguration(
                current.tableId(),
                current.propertyId(),
                current.tableNumber(),
                current.displayName(),
                current.floorName(),
                current.sectionName(),
                current.capacity(),
                status,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                current.active()
        );
    }

    private TableConfiguration applySettingsUpdate(TableConfiguration current, String propertyId, UpsertTableSettingRequest request) {
        TableStatus nextStatus = parseStatus(request.status());
        TableConfiguration updated = new TableConfiguration(
                current.tableId(),
                propertyId,
                request.tableNumber(),
                request.displayName(),
                request.floorName(),
                request.sectionName(),
                request.capacity(),
                nextStatus,
                current.waiterId(),
                current.cleanerId(),
                current.currentPartySize(),
                current.reservationPartySize(),
                current.reservationTime(),
                current.pendingStatus(),
                current.pendingStatusAt(),
                request.active()
        );
        if (nextStatus == TableStatus.UNAVAILABLE) {
            return clearRuntimeState(updated, TableStatus.UNAVAILABLE);
        }
        return updated;
    }

    private List<TableConfiguration> loadScopedConfigurations(String tenantId, String propertyId) {
        return restaurantTableRepository.findByTenantIdAndPropertyIdAndActiveTrue(tenantId, propertyId).stream()
                .map(entity -> mergeWithRuntime(tenantId, propertyId, entity))
                .toList();
    }

    private TableConfiguration getConfiguration(String tenantId, String propertyId, String tableId) {
        TableConfiguration runtime = tables.get(key(tenantId, propertyId, tableId));
        if (runtime != null) {
            return runtime;
        }
        return restaurantTableRepository.findByTenantIdAndPropertyIdAndTableId(tenantId, propertyId, tableId)
                .map(entity -> mergeWithRuntime(tenantId, propertyId, entity))
                .orElse(new TableConfiguration(
                        tableId,
                        propertyId,
                        tableId,
                        tableId,
                        "Main floor",
                        "Dining",
                        4,
                        TableStatus.AVAILABLE,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        true
                ));
    }

    private TableConfiguration mergeWithRuntime(String tenantId, String propertyId, RestaurantTableEntity entity) {
        TableConfiguration runtime = tables.get(key(tenantId, propertyId, entity.getTableId()));
        TableStatus persistedStatus = parseStatus(entity.getStatus());
        if (runtime == null) {
            return new TableConfiguration(
                    entity.getTableId(),
                    entity.getPropertyId(),
                    entity.getTableNumber(),
                    entity.getDisplayName() == null || entity.getDisplayName().isBlank() ? entity.getTableNumber() : entity.getDisplayName(),
                    entity.getFloorName(),
                    entity.getSectionName(),
                    entity.getCapacity(),
                    persistedStatus,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    entity.isActive()
            );
        }
        return new TableConfiguration(
                entity.getTableId(),
                entity.getPropertyId(),
                entity.getTableNumber(),
                entity.getDisplayName() == null || entity.getDisplayName().isBlank() ? runtime.displayName() : entity.getDisplayName(),
                entity.getFloorName(),
                entity.getSectionName(),
                entity.getCapacity(),
                runtime.status(),
                runtime.waiterId(),
                runtime.cleanerId(),
                runtime.currentPartySize(),
                runtime.reservationPartySize(),
                runtime.reservationTime(),
                runtime.pendingStatus(),
                runtime.pendingStatusAt(),
                entity.isActive()
        );
    }

    private void persistTableConfiguration(String tenantId, TableConfiguration configuration) {
        RestaurantTableEntity entity = restaurantTableRepository
                .findByTenantIdAndPropertyIdAndTableId(tenantId, configuration.propertyId(), configuration.tableId())
                .orElseGet(RestaurantTableEntity::new);
        entity.setTableId(configuration.tableId());
        entity.setTenantId(tenantId);
        entity.setPropertyId(configuration.propertyId());
        entity.setTableNumber(configuration.tableNumber());
        entity.setDisplayName(configuration.displayName());
        entity.setFloorName(configuration.floorName());
        entity.setSectionName(configuration.sectionName());
        entity.setCapacity(configuration.capacity());
        entity.setStatus(configuration.status().name());
        entity.setActive(configuration.active());
        restaurantTableRepository.save(entity);
    }

    private TableConfiguration syncDueTransition(String tenantId, String propertyId, TableConfiguration current) {
        if (current.pendingStatus() == null || current.pendingStatusAt() == null || current.pendingStatusAt().isAfter(Instant.now())) {
            return current;
        }

        TableConfiguration updated = switch (current.pendingStatus()) {
            case NEEDS_CLEANING -> new TableConfiguration(
                    current.tableId(),
                    current.propertyId(),
                    current.tableNumber(),
                    current.displayName(),
                    current.floorName(),
                    current.sectionName(),
                    current.capacity(),
                    TableStatus.NEEDS_CLEANING,
                    null,
                    current.cleanerId(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    current.active()
            );
            case AVAILABLE -> clearRuntimeState(new TableConfiguration(
                    current.tableId(),
                    current.propertyId(),
                    current.tableNumber(),
                    current.displayName(),
                    current.floorName(),
                    current.sectionName(),
                    current.capacity(),
                    TableStatus.AVAILABLE,
                    null,
                    current.cleanerId(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    current.active()
            ), TableStatus.AVAILABLE);
            default -> current;
        };

        tables.put(key(tenantId, propertyId, current.tableId()), updated);
        persistTableConfiguration(tenantId, updated);
        if (updated.status() != current.status()) {
            publishStatusChange(tenantId, toTableResponse(updated));
        }
        return updated;
    }

    private String scopePrefix(String tenantId, String propertyId) {
        return tenantId + "::" + propertyId + "::";
    }

    private String key(String tenantId, String propertyId, String tableId) {
        return scopePrefix(tenantId, propertyId) + tableId;
    }

    private TableResponse toTableResponse(TableConfiguration configuration) {
        return new TableResponse(
                configuration.tableId(),
                configuration.tableNumber(),
                configuration.displayName(),
                configuration.propertyId(),
                configuration.floorName(),
                configuration.sectionName(),
                configuration.capacity(),
                configuration.status(),
                configuration.waiterId(),
                configuration.cleanerId(),
                configuration.currentPartySize(),
                configuration.reservationPartySize(),
                configuration.reservationTime(),
                configuration.pendingStatus(),
                configuration.pendingStatusAt()
        );
    }

    private TableSettingRecordResponse toTableSettingRecord(TableConfiguration configuration) {
        return new TableSettingRecordResponse(
                configuration.tableId(),
                configuration.tableNumber(),
                configuration.displayName(),
                configuration.floorName(),
                configuration.sectionName(),
                configuration.capacity(),
                configuration.status().name(),
                configuration.active()
        );
    }

    private TableStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return TableStatus.AVAILABLE;
        }
        return TableStatus.valueOf(value.trim().toUpperCase());
    }

    private String normalize(String value) {
        return value.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }

    private void seed() {
        if (!restaurantTableRepository.findByTenantIdAndPropertyIdAndActiveTrue("bikini-bottom", "krusty-krab").isEmpty()) {
            return;
        }
        persistTableConfiguration("bikini-bottom", new TableConfiguration("table-01", "krusty-krab", "T-01", "Window 01", "Main floor", "Dining", 4, TableStatus.AVAILABLE, null, null, null, null, null, null, null, true));
        persistTableConfiguration("bikini-bottom", new TableConfiguration("table-02", "krusty-krab", "T-02", "Window 02", "Main floor", "Dining", 4, TableStatus.AVAILABLE, null, null, null, null, null, null, null, true));
        persistTableConfiguration("bikini-bottom", new TableConfiguration("table-03", "krusty-krab", "T-03", "Corner 03", "Main floor", "Patio", 2, TableStatus.AVAILABLE, null, null, null, null, null, null, null, true));
    }

    private record TableConfiguration(
            String tableId,
            String propertyId,
            String tableNumber,
            String displayName,
            String floorName,
            String sectionName,
            int capacity,
            TableStatus status,
            String waiterId,
            String cleanerId,
            Integer currentPartySize,
            Integer reservationPartySize,
            Instant reservationTime,
            TableStatus pendingStatus,
            Instant pendingStatusAt,
            boolean active
    ) {
    }
}
