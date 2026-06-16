package com.restaurant.billing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import com.restaurant.billing.persistence.entity.BillEntity;
import com.restaurant.billing.persistence.entity.BillItemEntity;
import com.restaurant.billing.persistence.entity.BillOrderEntity;
import com.restaurant.billing.persistence.repository.BillItemRepository;
import com.restaurant.billing.persistence.repository.BillOrderRepository;
import com.restaurant.billing.persistence.repository.BillRepository;
import com.restaurant.platform.eventing.contract.OrderCreatedEvent;
import com.restaurant.platform.eventing.contract.OrderLineItem;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BillingStoreTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private BillItemRepository billItemRepository;

    @Mock
    private BillOrderRepository billOrderRepository;

    private final Map<String, BillEntity> billsById = new LinkedHashMap<>();
    private final Map<String, List<BillItemEntity>> itemsByBillId = new LinkedHashMap<>();
    private final Map<String, List<BillOrderEntity>> ordersByBillId = new LinkedHashMap<>();

    private BillingStore billingStore;

    @BeforeEach
    void setUp() {
        billingStore = new BillingStore(billRepository, billItemRepository, billOrderRepository);

        when(billRepository.save(any(BillEntity.class))).thenAnswer(invocation -> {
            BillEntity entity = invocation.getArgument(0);
            if (entity.getGeneratedAt() == null) {
                entity.setGeneratedAt(Instant.now());
            }
            billsById.put(entity.getBillId(), entity);
            return entity;
        });
        when(billRepository.findById(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(billsById.get(invocation.getArgument(0))));
        when(billRepository.findByTenantIdAndPropertyIdOrderByGeneratedAtDesc(anyString(), anyString()))
                .thenAnswer(invocation -> billsById.values().stream()
                        .filter(bill -> invocation.getArgument(0).equals(bill.getTenantId()) && invocation.getArgument(1).equals(bill.getPropertyId()))
                        .sorted(Comparator.comparing(BillEntity::getGeneratedAt).reversed())
                        .toList());
        when(billRepository.findFirstByTenantIdAndPropertyIdAndSessionIdAndBillingStatusInOrderByGeneratedAtDesc(anyString(), anyString(), anyString(), anyList()))
                .thenAnswer(invocation -> findActiveBySession(
                        invocation.getArgument(0),
                        invocation.getArgument(1),
                        invocation.getArgument(2),
                        invocation.getArgument(3)
                ));
        when(billRepository.findFirstByTenantIdAndPropertyIdAndTableIdAndBillingStatusInOrderByGeneratedAtDesc(anyString(), anyString(), anyString(), anyList()))
                .thenAnswer(invocation -> findActiveByTable(
                        invocation.getArgument(0),
                        invocation.getArgument(1),
                        invocation.getArgument(2),
                        invocation.getArgument(3)
                ));

        doAnswer(invocation -> {
            itemsByBillId.remove(invocation.getArgument(0));
            return null;
        }).when(billItemRepository).deleteByBillId(anyString());
        when(billItemRepository.saveAll(anyCollection())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            List<BillItemEntity> entities = new ArrayList<>((Collection<BillItemEntity>) invocation.getArgument(0));
            if (!entities.isEmpty()) {
                itemsByBillId.put(entities.get(0).getBillId(), entities);
            }
            return entities;
        });
        when(billItemRepository.findByBillIdOrderByBillItemIdAsc(anyString()))
                .thenAnswer(invocation -> itemsByBillId.getOrDefault(invocation.getArgument(0), List.of()));
        when(billItemRepository.findByBillIdIn(anyCollection()))
                .thenAnswer(invocation -> flattenByIds(itemsByBillId, invocation.getArgument(0)));

        doAnswer(invocation -> {
            ordersByBillId.remove(invocation.getArgument(0));
            return null;
        }).when(billOrderRepository).deleteByBillId(anyString());
        when(billOrderRepository.saveAll(anyCollection())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            List<BillOrderEntity> entities = new ArrayList<>((Collection<BillOrderEntity>) invocation.getArgument(0));
            if (!entities.isEmpty()) {
                ordersByBillId.put(entities.get(0).getBillId(), entities);
            }
            return entities;
        });
        when(billOrderRepository.findByBillIdOrderByAttachedAtAsc(anyString()))
                .thenAnswer(invocation -> ordersByBillId.getOrDefault(invocation.getArgument(0), List.of()));
        when(billOrderRepository.findByBillIdIn(anyCollection()))
                .thenAnswer(invocation -> flattenByIds(ordersByBillId, invocation.getArgument(0)));
    }

    @Test
    void createDraftFromOrderCreatesNewDraftWithSessionLinkAndOrderLineItems() {
        BillRecord record = billingStore.createDraftFromOrder(orderEvent(
                "order-001",
                "session-001",
                "table-001",
                List.of(
                        new OrderLineItem("item-001", "Margherita Pizza", 2),
                        new OrderLineItem("item-002", "Pasta Alfredo", 1)
                )
        ));

        assertThat(record.sessionId()).isEqualTo("session-001");
        assertThat(record.lastOrderId()).isEqualTo("order-001");
        assertThat(record.orderIds()).containsExactly("order-001");
        assertThat(record.items())
                .extracting(BillLineRecord::quantity)
                .containsExactlyInAnyOrder(2, 1);
        assertThat(record.subtotal()).isEqualByComparingTo("847.00");
        assertThat(record.tax()).isEqualByComparingTo("42.35");
        assertThat(record.total()).isEqualByComparingTo("889.35");
    }

    @Test
    void createDraftFromOrderAppendsNewOrderToExistingBillAndMergesMenuItems() {
        BillRecord initial = billingStore.createDraftFromOrder(orderEvent(
                "order-001",
                "session-001",
                "table-001",
                List.of(new OrderLineItem("item-001", "Margherita Pizza", 1))
        ));

        BillRecord appended = billingStore.createDraftFromOrder(orderEvent(
                "order-002",
                "session-001",
                "table-001",
                List.of(
                        new OrderLineItem("item-001", "Margherita Pizza", 2),
                        new OrderLineItem("item-002", "Pasta Alfredo", 1)
                )
        ));

        assertThat(appended.billId()).isEqualTo(initial.billId());
        assertThat(appended.lastOrderId()).isEqualTo("order-002");
        assertThat(appended.orderIds()).containsExactly("order-001", "order-002");
        assertThat(appended.items())
                .filteredOn(item -> item.itemId().equals("item-001"))
                .singleElement()
                .extracting(BillLineRecord::quantity)
                .isEqualTo(3);
    }

    @Test
    void finalizeCancellationBillUsesCancellationFeeAsFinalCharge() {
        BillRecord initial = billingStore.createDraftFromOrder(orderEvent(
                "order-003",
                "session-003",
                "table-003",
                List.of(new OrderLineItem("item-002", "Pasta Alfredo", 1))
        ));

        BillRecord cancelled = billingStore.finalizeCancellationBill(
                "bikini-bottom",
                "krusty-krab",
                initial.billId(),
                new FinalizeCancellationBillRequest("Customer left early", BigDecimal.valueOf(125))
        );

        assertThat(cancelled.status()).isEqualTo("FINALIZED");
        assertThat(cancelled.settlementType()).isEqualTo(BillSettlementType.CANCELLATION.name());
        assertThat(cancelled.cancellationReason()).isEqualTo("Customer left early");
        assertThat(cancelled.cancellationFee()).isEqualByComparingTo("125.00");
        assertThat(cancelled.subtotal()).isEqualByComparingTo("125.00");
        assertThat(cancelled.tax()).isEqualByComparingTo("0.00");
        assertThat(cancelled.total()).isEqualByComparingTo("125.00");
    }

    @Test
    void attachCustomerAndMarkPaidUpdateTheActiveBill() {
        BillRecord initial = billingStore.createDraftFromOrder(orderEvent(
                "order-004",
                "session-004",
                "table-004",
                List.of(new OrderLineItem("item-001", "Margherita Pizza", 1))
        ));

        BillRecord attached = billingStore.attachCustomer("bikini-bottom", "krusty-krab", initial.billId(), "cust-004");
        BillRecord paid = billingStore.markPaid(initial.billId());

        assertThat(attached.customerId()).isEqualTo("cust-004");
        assertThat(paid.status()).isEqualTo("PAID");
    }

    private OrderCreatedEvent orderEvent(String orderId, String sessionId, String tableId, List<OrderLineItem> items) {
        return new OrderCreatedEvent(
                orderId,
                "bikini-bottom",
                "krusty-krab",
                tableId,
                sessionId,
                "emp-01",
                null,
                items,
                Instant.parse("2026-06-14T09:00:00Z")
        );
    }

    private Optional<BillEntity> findActiveBySession(String tenantId, String propertyId, String sessionId, List<String> statuses) {
        return billsById.values().stream()
                .filter(bill -> tenantId.equals(bill.getTenantId()))
                .filter(bill -> propertyId.equals(bill.getPropertyId()))
                .filter(bill -> sessionId.equals(bill.getSessionId()))
                .filter(bill -> statuses.contains(bill.getBillingStatus()))
                .max(Comparator.comparing(BillEntity::getGeneratedAt));
    }

    private Optional<BillEntity> findActiveByTable(String tenantId, String propertyId, String tableId, List<String> statuses) {
        return billsById.values().stream()
                .filter(bill -> tenantId.equals(bill.getTenantId()))
                .filter(bill -> propertyId.equals(bill.getPropertyId()))
                .filter(bill -> tableId.equals(bill.getTableId()))
                .filter(bill -> statuses.contains(bill.getBillingStatus()))
                .max(Comparator.comparing(BillEntity::getGeneratedAt));
    }

    private <T> List<T> flattenByIds(Map<String, List<T>> source, Object rawIds) {
        @SuppressWarnings("unchecked")
        Collection<String> billIds = (Collection<String>) rawIds;
        return billIds.stream()
                .flatMap(billId -> source.getOrDefault(billId, List.of()).stream())
                .toList();
    }
}
