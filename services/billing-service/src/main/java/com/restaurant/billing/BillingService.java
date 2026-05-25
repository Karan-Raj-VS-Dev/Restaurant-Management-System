package com.restaurant.billing;

import com.restaurant.platform.eventing.AggregateTypes;
import com.restaurant.platform.eventing.DomainEventPublisher;
import com.restaurant.platform.eventing.EventEnvelopeFactory;
import com.restaurant.platform.eventing.EventKeys;
import com.restaurant.platform.eventing.contract.BillDraftedEvent;
import com.restaurant.platform.eventing.contract.BillFinalizedEvent;
import com.restaurant.platform.eventing.contract.OrderCreatedEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class BillingService {

    private final BillingStore billingStore;
    private final EventEnvelopeFactory eventEnvelopeFactory;
    private final DomainEventPublisher domainEventPublisher;

    public BillingService(BillingStore billingStore,
                          EventEnvelopeFactory eventEnvelopeFactory,
                          DomainEventPublisher domainEventPublisher) {
        this.billingStore = billingStore;
        this.eventEnvelopeFactory = eventEnvelopeFactory;
        this.domainEventPublisher = domainEventPublisher;
    }

    public List<BillResponse> listBills(String tenantId, String propertyId) {
        return billingStore.listBills(tenantId, propertyId).stream()
                .map(this::toResponse)
                .toList();
    }

    public BillResponse createDraftBill(String tenantId, String propertyId, DraftBillRequest request) {
        BillRecord bill = billingStore.createManualDraft(
                tenantId,
                propertyId,
                request.orderId(),
                request.items().stream()
                        .map(item -> new BillLineRecord(item.itemId(), item.itemName(), item.quantity(), item.unitPrice()))
                        .toList()
        );
        publishDraftedEvent(bill, bill.orderId(), null);
        return toResponse(bill);
    }

    public BillResponse finalizeBill(String tenantId, String propertyId, String billId) {
        BillRecord bill = billingStore.finalizeBill(tenantId, propertyId, billId);
        domainEventPublisher.publish(eventEnvelopeFactory.create(
                EventKeys.BILL_FINALIZED,
                AggregateTypes.BILL,
                bill.billId(),
                bill.orderId(),
                bill.orderId(),
                null,
                new BillFinalizedEvent(bill.billId(), bill.orderId(), bill.tenantId(), bill.propertyId(), bill.total(), Instant.now())
        ));
        return toResponse(bill);
    }

    public BillResponse finalizeBillCancellation(String tenantId, String propertyId, String billId, FinalizeCancellationBillRequest request) {
        BillRecord bill = billingStore.finalizeCancellationBill(tenantId, propertyId, billId, request);
        domainEventPublisher.publish(eventEnvelopeFactory.create(
                EventKeys.BILL_FINALIZED,
                AggregateTypes.BILL,
                bill.billId(),
                bill.orderId(),
                bill.orderId(),
                null,
                new BillFinalizedEvent(bill.billId(), bill.orderId(), bill.tenantId(), bill.propertyId(), bill.total(), Instant.now())
        ));
        return toResponse(bill);
    }

    public BillResponse getBill(String tenantId, String propertyId, String billId) {
        return toResponse(billingStore.getBill(tenantId, propertyId, billId));
    }

    public void draftBillFromOrder(OrderCreatedEvent event, String causationId) {
        BillRecord bill = billingStore.createDraftFromOrder(event);
        publishDraftedEvent(bill, event.orderId(), causationId);
    }

    public void markBillPaid(String billId) {
        billingStore.markPaid(billId);
    }

    public void publishDraftedEvent(BillRecord bill, String partitionKey, String causationId) {
        domainEventPublisher.publish(eventEnvelopeFactory.create(
                EventKeys.BILL_DRAFTED,
                AggregateTypes.BILL,
                bill.billId(),
                partitionKey,
                partitionKey,
                causationId,
                new BillDraftedEvent(bill.billId(), bill.orderId(), bill.tenantId(), bill.propertyId(), bill.total(), Instant.now())
        ));
    }

    private BillResponse toResponse(BillRecord bill) {
        return new BillResponse(
                bill.billId(),
                bill.orderId(),
                bill.orderIds(),
                bill.tableId(),
                BillStatus.valueOf(bill.status()),
                BillSettlementType.valueOf(bill.settlementType()),
                bill.cancellationReason(),
                bill.cancellationFee(),
                bill.items().stream()
                        .map(item -> new BillLine(item.itemId(), item.itemName(), item.quantity(), item.unitPrice()))
                        .toList(),
                bill.subtotal(),
                bill.tax(),
                bill.total()
        );
    }
}
