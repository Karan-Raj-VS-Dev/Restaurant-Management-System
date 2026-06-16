package com.restaurant.billing;

import com.restaurant.billing.persistence.entity.BillEntity;
import com.restaurant.billing.persistence.entity.BillItemEntity;
import com.restaurant.billing.persistence.entity.BillOrderEntity;
import com.restaurant.billing.persistence.repository.BillItemRepository;
import com.restaurant.billing.persistence.repository.BillOrderRepository;
import com.restaurant.billing.persistence.repository.BillRepository;
import com.restaurant.platform.eventing.contract.OrderCreatedEvent;
import com.restaurant.platform.eventing.contract.OrderLineItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Component
@Transactional
public class BillingStore {

    private static final BigDecimal DEFAULT_TAX_RATE = BigDecimal.valueOf(0.05);
    private static final BigDecimal ZERO_AMOUNT = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final List<String> ACTIVE_BILL_STATUSES = List.of("DRAFT", "FINALIZED");
    private static final Map<String, BigDecimal> MENU_PRICES = Map.of(
            "item-001", BigDecimal.valueOf(299),
            "item-002", BigDecimal.valueOf(249),
            "item-003", BigDecimal.valueOf(199)
    );

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final BillOrderRepository billOrderRepository;

    public BillingStore(BillRepository billRepository,
                        BillItemRepository billItemRepository,
                        BillOrderRepository billOrderRepository) {
        this.billRepository = billRepository;
        this.billItemRepository = billItemRepository;
        this.billOrderRepository = billOrderRepository;
    }

    public BillRecord createDraftFromOrder(OrderCreatedEvent event) {
        List<BillLineRecord> lines = event.items().stream()
                .map(this::toLine)
                .toList();
        BillEntity existing = findActiveBill(event.tenantId(), event.propertyId(), event.sessionId(), event.tableId());
        if (existing != null) {
            return appendOrderToBill(existing, event.orderId(), event.sessionId(), lines);
        }
        return createDraft("bill-" + UUID.randomUUID(), event.orderId(), event.tenantId(), event.propertyId(), event.tableId(), event.sessionId(), lines);
    }

    public BillRecord createManualDraft(String tenantId, String propertyId, String orderId, String sessionId, List<BillLineRecord> lines) {
        return createDraft("bill-" + UUID.randomUUID(), orderId, tenantId, propertyId, null, sessionId, lines);
    }

    public BillRecord finalizeBill(String tenantId, String propertyId, String billId) {
        BillEntity entity = billRepository.findById(billId)
                .filter(bill -> tenantId.equals(bill.getTenantId()) && propertyId.equals(bill.getPropertyId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill was not found."));
        entity.setBillingStatus("FINALIZED");
        entity.setSettlementType(BillSettlementType.STANDARD.name());
        BillEntity saved = billRepository.save(entity);
        return toRecord(
                saved,
                billOrderRepository.findByBillIdOrderByAttachedAtAsc(billId),
                billItemRepository.findByBillIdOrderByBillItemIdAsc(billId)
        );
    }

    public BillRecord attachCustomer(String tenantId, String propertyId, String billId, String customerId) {
        BillEntity entity = billRepository.findById(billId)
                .filter(bill -> tenantId.equals(bill.getTenantId()) && propertyId.equals(bill.getPropertyId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill was not found."));
        entity.setCustomerId(blankToNull(customerId));
        BillEntity saved = billRepository.save(entity);
        return toRecord(
                saved,
                billOrderRepository.findByBillIdOrderByAttachedAtAsc(billId),
                billItemRepository.findByBillIdOrderByBillItemIdAsc(billId)
        );
    }

    public BillRecord finalizeCancellationBill(String tenantId, String propertyId, String billId, FinalizeCancellationBillRequest request) {
        BillEntity entity = billRepository.findById(billId)
                .filter(bill -> tenantId.equals(bill.getTenantId()) && propertyId.equals(bill.getPropertyId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill was not found."));
        BigDecimal fee = scale(request.cancellationFee());
        entity.setBillingStatus("FINALIZED");
        entity.setSettlementType(BillSettlementType.CANCELLATION.name());
        entity.setCancellationReason(request.reason().trim());
        entity.setCancellationFeeAmount(fee);
        entity.setSubtotalAmount(fee);
        entity.setTaxAmount(ZERO_AMOUNT);
        entity.setServiceChargeAmount(ZERO_AMOUNT);
        entity.setDiscountAmount(ZERO_AMOUNT);
        entity.setTotalAmount(fee);
        entity.setClosedAt(null);
        BillEntity saved = billRepository.save(entity);
        return toRecord(
                saved,
                billOrderRepository.findByBillIdOrderByAttachedAtAsc(billId),
                billItemRepository.findByBillIdOrderByBillItemIdAsc(billId)
        );
    }

    public BillRecord getBill(String tenantId, String propertyId, String billId) {
        BillEntity entity = billRepository.findById(billId)
                .filter(bill -> tenantId.equals(bill.getTenantId()) && propertyId.equals(bill.getPropertyId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill was not found."));
        return toRecord(
                entity,
                billOrderRepository.findByBillIdOrderByAttachedAtAsc(billId),
                billItemRepository.findByBillIdOrderByBillItemIdAsc(billId)
        );
    }

    public BillRecord markPaid(String billId) {
        BillEntity entity = billRepository.findById(billId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill was not found."));
        entity.setBillingStatus("PAID");
        entity.setClosedAt(Instant.now());
        BillEntity saved = billRepository.save(entity);
        return toRecord(
                saved,
                billOrderRepository.findByBillIdOrderByAttachedAtAsc(billId),
                billItemRepository.findByBillIdOrderByBillItemIdAsc(billId)
        );
    }

    public List<BillRecord> listBills(String tenantId, String propertyId) {
        List<BillEntity> bills = billRepository.findByTenantIdAndPropertyIdOrderByGeneratedAtDesc(tenantId, propertyId);
        Map<String, List<BillOrderEntity>> ordersByBillId = loadOrdersByBillId(
                bills.stream().map(BillEntity::getBillId).toList()
        );
        Map<String, List<BillItemEntity>> itemsByBillId = loadItemsByBillId(
                bills.stream().map(BillEntity::getBillId).toList()
        );
        return bills.stream()
                .map(bill -> toRecord(
                        bill,
                        ordersByBillId.getOrDefault(bill.getBillId(), List.of()),
                        itemsByBillId.getOrDefault(bill.getBillId(), List.of())
                ))
                .toList();
    }

    private BillRecord createDraft(
            String billId,
            String orderId,
            String tenantId,
            String propertyId,
            String tableId,
            String sessionId,
            List<BillLineRecord> items
    ) {
        BillSnapshot snapshot = summarize(items);
        BillEntity entity = new BillEntity();
        entity.setBillId(billId);
        entity.setLastOrderId(orderId);
        entity.setTenantId(tenantId);
        entity.setPropertyId(propertyId);
        entity.setTableId(tableId);
        entity.setSessionId(blankToNull(sessionId));
        entity.setCustomerId(null);
        entity.setBillingStatus("DRAFT");
        entity.setSettlementType(BillSettlementType.STANDARD.name());
        entity.setCancellationReason(null);
        entity.setCancellationFeeAmount(ZERO_AMOUNT);
        entity.setSubtotalAmount(snapshot.subtotal());
        entity.setTaxAmount(snapshot.tax());
        entity.setServiceChargeAmount(ZERO_AMOUNT);
        entity.setDiscountAmount(ZERO_AMOUNT);
        entity.setTotalAmount(snapshot.total());
        entity.setGeneratedAt(Instant.now());
        BillEntity saved = billRepository.save(entity);
        replaceBillOrders(saved.getBillId(), List.of(orderId));
        replaceBillItems(saved.getBillId(), items);
        return toRecord(
                saved,
                billOrderRepository.findByBillIdOrderByAttachedAtAsc(saved.getBillId()),
                billItemRepository.findByBillIdOrderByBillItemIdAsc(saved.getBillId())
        );
    }

    private BillEntity findActiveBill(String tenantId, String propertyId, String sessionId, String tableId) {
        if (sessionId != null && !sessionId.isBlank()) {
            BillEntity sessionBill = billRepository.findFirstByTenantIdAndPropertyIdAndSessionIdAndBillingStatusInOrderByGeneratedAtDesc(
                    tenantId,
                    propertyId,
                    sessionId,
                    ACTIVE_BILL_STATUSES
            ).orElse(null);
            if (sessionBill != null) {
                return sessionBill;
            }
        }
        return findActiveBillForTable(tenantId, propertyId, tableId);
    }

    private BillEntity findActiveBillForTable(String tenantId, String propertyId, String tableId) {
        if (tableId == null || tableId.isBlank()) {
            return null;
        }
        return billRepository.findFirstByTenantIdAndPropertyIdAndTableIdAndBillingStatusInOrderByGeneratedAtDesc(
                tenantId,
                propertyId,
                tableId,
                ACTIVE_BILL_STATUSES
        ).orElse(null);
    }

    private BillRecord appendOrderToBill(BillEntity existing, String orderId, String sessionId, List<BillLineRecord> additionalLines) {
        List<BillLineRecord> mergedItems = mergeLineItems(
                billItemRepository.findByBillIdOrderByBillItemIdAsc(existing.getBillId()).stream()
                        .map(this::toLineRecord)
                        .toList(),
                additionalLines
        );
        BillSnapshot snapshot = summarize(mergedItems);
        List<String> orderIds = new ArrayList<>(loadOrderIdsForBill(existing.getBillId()));
        if (!orderIds.contains(orderId)) {
            orderIds.add(orderId);
        }
        existing.setLastOrderId(orderId);
        if (blankToNull(existing.getSessionId()) == null && blankToNull(sessionId) != null) {
            existing.setSessionId(blankToNull(sessionId));
        }
        existing.setBillingStatus("DRAFT");
        existing.setSettlementType(BillSettlementType.STANDARD.name());
        existing.setCancellationReason(null);
        existing.setCancellationFeeAmount(ZERO_AMOUNT);
        existing.setSubtotalAmount(snapshot.subtotal());
        existing.setTaxAmount(snapshot.tax());
        existing.setTotalAmount(snapshot.total());
        existing.setClosedAt(null);
        BillEntity saved = billRepository.save(existing);
        replaceBillOrders(saved.getBillId(), orderIds);
        replaceBillItems(saved.getBillId(), mergedItems);
        return toRecord(
                saved,
                billOrderRepository.findByBillIdOrderByAttachedAtAsc(saved.getBillId()),
                billItemRepository.findByBillIdOrderByBillItemIdAsc(saved.getBillId())
        );
    }

    private void replaceBillOrders(String billId, List<String> orderIds) {
        billOrderRepository.deleteByBillId(billId);
        List<BillOrderEntity> entities = orderIds.stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .map(orderId -> {
                    BillOrderEntity entity = new BillOrderEntity();
                    entity.setBillId(billId);
                    entity.setOrderId(orderId);
                    return entity;
                })
                .toList();
        if (!entities.isEmpty()) {
            billOrderRepository.saveAll(entities);
        }
    }

    private void replaceBillItems(String billId, List<BillLineRecord> items) {
        billItemRepository.deleteByBillId(billId);
        if (items.isEmpty()) {
            return;
        }
        List<BillItemEntity> entities = items.stream()
                .map(item -> {
                    BillItemEntity entity = new BillItemEntity();
                    BigDecimal unitPrice = scale(item.unitPrice());
                    BigDecimal lineSubtotal = unitPrice.multiply(BigDecimal.valueOf(item.quantity())).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal lineTax = lineSubtotal.multiply(DEFAULT_TAX_RATE).setScale(2, RoundingMode.HALF_UP);
                    entity.setBillItemId("bill-item-" + UUID.randomUUID());
                    entity.setBillId(billId);
                    entity.setMenuItemId(item.itemId());
                    entity.setItemName(item.itemName());
                    entity.setQuantity(item.quantity());
                    entity.setUnitPrice(unitPrice);
                    entity.setTaxAmount(lineTax);
                    entity.setLineTotal(lineSubtotal.add(lineTax));
                    return entity;
                })
                .toList();
        billItemRepository.saveAll(entities);
    }

    private Map<String, List<BillItemEntity>> loadItemsByBillId(Collection<String> billIds) {
        if (billIds.isEmpty()) {
            return Map.of();
        }
        return billItemRepository.findByBillIdIn(billIds).stream()
                .collect(Collectors.groupingBy(BillItemEntity::getBillId));
    }

    private Map<String, List<BillOrderEntity>> loadOrdersByBillId(Collection<String> billIds) {
        if (billIds.isEmpty()) {
            return Map.of();
        }
        return billOrderRepository.findByBillIdIn(billIds).stream()
                .collect(Collectors.groupingBy(BillOrderEntity::getBillId));
    }

    private List<String> loadOrderIdsForBill(String billId) {
        return billOrderRepository.findByBillIdOrderByAttachedAtAsc(billId).stream()
                .map(BillOrderEntity::getOrderId)
                .distinct()
                .toList();
    }

    private BillRecord toRecord(BillEntity bill, List<BillOrderEntity> billOrders, List<BillItemEntity> items) {
        List<String> orderIds = billOrders.stream()
                .map(BillOrderEntity::getOrderId)
                .distinct()
                .toList();
        if (orderIds.isEmpty() && bill.getLastOrderId() != null && !bill.getLastOrderId().isBlank()) {
            orderIds = List.of(bill.getLastOrderId());
        }
        return new BillRecord(
                bill.getBillId(),
                bill.getLastOrderId(),
                orderIds,
                bill.getTenantId(),
                bill.getPropertyId(),
                bill.getTableId(),
                bill.getSessionId(),
                bill.getCustomerId(),
                bill.getBillingStatus(),
                bill.getSettlementType(),
                bill.getCancellationReason(),
                scale(bill.getCancellationFeeAmount()),
                items.stream().map(this::toLineRecord).toList(),
                scale(bill.getSubtotalAmount()),
                scale(bill.getTaxAmount()),
                scale(bill.getTotalAmount())
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

    private BillLineRecord toLineRecord(BillItemEntity item) {
        return new BillLineRecord(
                item.getMenuItemId(),
                item.getItemName(),
                item.getQuantity(),
                scale(item.getUnitPrice())
        );
    }

    private List<BillLineRecord> mergeLineItems(List<BillLineRecord> currentItems, List<BillLineRecord> additionalItems) {
        Map<String, BillLineRecord> merged = new LinkedHashMap<>();
        currentItems.forEach(item -> merged.put(item.itemId(), item));
        additionalItems.forEach(item -> merged.compute(
                item.itemId(),
                (key, existing) -> existing == null
                        ? item
                        : new BillLineRecord(
                                existing.itemId(),
                                existing.itemName(),
                                existing.quantity() + item.quantity(),
                                existing.unitPrice()
                        )
        ));
        return List.copyOf(merged.values());
    }

    private BillSnapshot summarize(List<BillLineRecord> items) {
        BigDecimal subtotal = items.stream()
                .map(item -> scale(item.unitPrice()).multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = subtotal.multiply(DEFAULT_TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);
        return new BillSnapshot(subtotal, tax, total);
    }

    private BigDecimal scale(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : value.setScale(2, RoundingMode.HALF_UP);
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record BillSnapshot(BigDecimal subtotal, BigDecimal tax, BigDecimal total) {
    }
}
