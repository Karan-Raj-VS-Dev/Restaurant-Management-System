package com.restaurant.table;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.restaurant.platform.eventing.DomainEventPublisher;
import com.restaurant.platform.eventing.EventEnvelopeFactory;
import com.restaurant.table.persistence.entity.RestaurantTableEntity;
import com.restaurant.table.persistence.entity.TableSessionEntity;
import com.restaurant.table.persistence.repository.RestaurantTableRepository;
import com.restaurant.table.persistence.repository.TableSessionRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TableServiceTest {

    @Mock
    private RestaurantTableRepository restaurantTableRepository;

    @Mock
    private TableSessionRepository tableSessionRepository;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    private final Map<String, RestaurantTableEntity> tablesById = new LinkedHashMap<>();
    private final Map<String, TableSessionEntity> openSessionsByTableId = new LinkedHashMap<>();

    private TableService tableService;

    @BeforeEach
    void setUp() {
        when(restaurantTableRepository.findByTenantIdAndPropertyIdAndTableId(anyString(), anyString(), anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(tablesById.get(invocation.getArgument(2))));
        when(restaurantTableRepository.findByTenantIdAndPropertyIdAndActiveTrue(anyString(), anyString()))
                .thenAnswer(invocation -> tablesById.values().stream()
                        .filter(entity -> invocation.getArgument(0).equals(entity.getTenantId())
                                && invocation.getArgument(1).equals(entity.getPropertyId())
                                && entity.isActive())
                        .toList());
        when(restaurantTableRepository.save(any(RestaurantTableEntity.class))).thenAnswer(invocation -> {
            RestaurantTableEntity entity = invocation.getArgument(0);
            tablesById.put(entity.getTableId(), entity);
            return entity;
        });

        when(tableSessionRepository.findFirstByTenantIdAndPropertyIdAndTableIdAndSessionStatusOrderByStartedAtDesc(
                anyString(), anyString(), anyString(), anyString()
        )).thenAnswer(invocation -> Optional.ofNullable(openSessionsByTableId.get(invocation.getArgument(2))));
        when(tableSessionRepository.save(any(TableSessionEntity.class))).thenAnswer(invocation -> {
            TableSessionEntity entity = invocation.getArgument(0);
            if ("OPEN".equals(entity.getSessionStatus())) {
                openSessionsByTableId.put(entity.getTableId(), entity);
            } else {
                openSessionsByTableId.remove(entity.getTableId());
            }
            return entity;
        });

        tableService = new TableService(
                restaurantTableRepository,
                tableSessionRepository,
                new EventEnvelopeFactory("test-suite"),
                domainEventPublisher
        );
    }

    @Test
    void updateStatusToOccupiedOpensSessionAndPersistsWaiterAndPartySize() {
        TableResponse response = tableService.updateStatus(
                "bikini-bottom",
                "table-01",
                "krusty-krab",
                new UpdateTableStatusRequest(
                        TableStatus.OCCUPIED.name(),
                        3,
                        "emp-101",
                        null,
                        null,
                        null,
                        true,
                        false
                )
        );

        assertThat(response.status()).isEqualTo(TableStatus.OCCUPIED);
        assertThat(response.waiterId()).isEqualTo("emp-101");
        assertThat(response.currentPartySize()).isEqualTo(3);
        assertThat(response.sessionId()).startsWith("session-");
        assertThat(openSessionsByTableId).containsKey("table-01");

        ArgumentCaptor<TableSessionEntity> captor = ArgumentCaptor.forClass(TableSessionEntity.class);
        verify(tableSessionRepository).save(captor.capture());
        assertThat(captor.getValue().getAssignedWaiterId()).isEqualTo("emp-101");
        assertThat(captor.getValue().getCustomerCount()).isEqualTo(3);
    }

    @Test
    void attachOrderToOpenSessionStoresOrderIdOnSessionAndResponse() {
        tableService.updateStatus(
                "bikini-bottom",
                "table-01",
                "krusty-krab",
                new UpdateTableStatusRequest(
                        TableStatus.OCCUPIED.name(),
                        2,
                        "emp-101",
                        null,
                        null,
                        null,
                        true,
                        false
                )
        );

        TableResponse response = tableService.attachOrderToOpenSession(
                "bikini-bottom",
                "krusty-krab",
                "table-01",
                "order-123"
        );

        assertThat(response.orderId()).isEqualTo("order-123");
        assertThat(openSessionsByTableId.get("table-01").getOrderId()).isEqualTo("order-123");
    }

    @Test
    void attachCustomerToOpenSessionStoresCustomerIdOnSessionAndResponse() {
        tableService.updateStatus(
                "bikini-bottom",
                "table-01",
                "krusty-krab",
                new UpdateTableStatusRequest(
                        TableStatus.OCCUPIED.name(),
                        2,
                        "emp-101",
                        null,
                        null,
                        null,
                        true,
                        false
                )
        );

        TableResponse response = tableService.attachCustomerToOpenSession(
                "bikini-bottom",
                "krusty-krab",
                "table-01",
                "cust-123"
        );

        assertThat(response.customerId()).isEqualTo("cust-123");
        assertThat(openSessionsByTableId.get("table-01").getCustomerId()).isEqualTo("cust-123");
    }

    @Test
    void scheduleNeedsCleaningAfterPaymentKeepsOpenSessionUntilCleaningTransitionRuns() {
        TableResponse occupied = tableService.updateStatus(
                "bikini-bottom",
                "table-01",
                "krusty-krab",
                new UpdateTableStatusRequest(
                        TableStatus.OCCUPIED.name(),
                        4,
                        "emp-101",
                        null,
                        null,
                        null,
                        true,
                        false
                )
        );

        TableResponse response = tableService.scheduleNeedsCleaningAfterPayment(
                "bikini-bottom",
                "krusty-krab",
                "table-01"
        );

        assertThat(occupied.sessionId()).isNotNull();
        assertThat(response.status()).isEqualTo(TableStatus.OCCUPIED);
        assertThat(response.pendingStatus()).isEqualTo(TableStatus.NEEDS_CLEANING);
        assertThat(response.pendingStatusAt()).isNotNull();
        assertThat(response.sessionId()).isEqualTo(occupied.sessionId());
        assertThat(openSessionsByTableId).containsKey("table-01");
    }
}
