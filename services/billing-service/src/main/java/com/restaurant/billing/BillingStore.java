package com.restaurant.billing;

import com.restaurant.platform.eventing.contract.OrderCreatedEvent;
import com.restaurant.platform.eventing.contract.OrderLineItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BillingStore {

    private static final Map<String, BigDecimal> MENU_PRICES = Map.of(
            "item-001", BigDecimal.valueOf(299),
            "item-002", BigDecimal.valueOf(249),
            "item-003", BigDecimal.valueOf(199)
    );

    private final Map<String, BillRecord> bills = new ConcurrentHashMap<>();

    public BillRecord createDraftFromOrder(OrderCreatedEvent event) {
        List<BillLineRecord> lines = event.items().stream()
                .map(this::toLine)
                .toList();
        return createDraft("bill-" + UUID.randomUUID(), event.orderId(), event.tenantId(), event.propertyId(), event.tableId(), lines);
    }

    public BillRecord createManualDraft(String tenantId, String propertyId, String orderId, List<BillLineRecord> lines) {
        return createDraft("bill-" + UUID.randomUUID(), orderId, tenantId, propertyId, null, lines);
    }

    public BillRecord finalizeBill(String tenantId, String propertyId, String billId) {
        return bills.compute(billId, (key, existing) -> {
            BillRecord current = existing != null
                    ? existing
                    : buildDraft(billId, "order-unknown", tenantId, propertyId, null, List.of(new BillLineRecord("item-001", "Margherita Pizza", 1, BigDecimal.valueOf(299))));
            return new BillRecord(
                    current.billId(),
                    current.orderId(),
                    current.tenantId(),
                    current.propertyId(),
                    current.tableId(),
                    "FINALIZED",
                    current.items(),
                    current.subtotal(),
                    current.tax(),
                    current.total()
            );
        });
    }

    public BillRecord getBill(String tenantId, String propertyId, String billId) {
        return bills.computeIfAbsent(
                billId,
                ignored -> buildDraft("bill-fallback", "order-fallback", tenantId, propertyId, null, List.of(new BillLineRecord("item-001", "Margherita Pizza", 1, BigDecimal.valueOf(299))))
        );
    }

    public BillRecord markPaid(String billId) {
        return bills.computeIfPresent(billId, (ignored, current) -> new BillRecord(
                current.billId(),
                current.orderId(),
                current.tenantId(),
                current.propertyId(),
                current.tableId(),
                "PAID",
                current.items(),
                current.subtotal(),
                current.tax(),
                current.total()
        ));
    }

    public List<BillRecord> listBills(String tenantId, String propertyId) {
        return bills.values().stream()
                .filter(bill -> tenantId.equals(bill.tenantId()) && propertyId.equals(bill.propertyId()))
                .sorted((left, right) -> right.billId().compareTo(left.billId()))
                .toList();
    }

    private BillRecord createDraft(String billId, String orderId, String tenantId, String propertyId, String tableId, List<BillLineRecord> items) {
        BillRecord record = buildDraft(billId, orderId, tenantId, propertyId, tableId, items);
        bills.put(record.billId(), record);
        return record;
    }

    private BillRecord buildDraft(String billId, String orderId, String tenantId, String propertyId, String tableId, List<BillLineRecord> items) {
        BigDecimal subtotal = items.stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.05));
        return new BillRecord(
                billId,
                orderId,
                tenantId,
                propertyId,
                tableId,
                "DRAFT",
                items,
                subtotal,
                tax,
                subtotal.add(tax)
        );
    }

    private BillLineRecord toLine(OrderLineItem item) {
        return new BillLineRecord(
                item.itemId(),
                item.itemName(),
                item.quantity(),
                MENU_PRICES.getOrDefault(item.itemId(), BigDecimal.valueOf(199))
        );
    }

}
