package com.restaurant.insights;

import com.restaurant.platform.eventing.contract.OrderCreatedEvent;
import com.restaurant.platform.eventing.contract.PaymentSucceededEvent;
import com.restaurant.platform.eventing.contract.StockAlertEvent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperationsInsightsService {

    private final OperationsInsightsProjection operationsInsightsProjection;

    public OperationsInsightsService(OperationsInsightsProjection operationsInsightsProjection) {
        this.operationsInsightsProjection = operationsInsightsProjection;
    }

    public DailyOperationsInsight getDailyInsights(String tenantId, String propertyId) {
        DailySnapshot snapshot = operationsInsightsProjection.snapshot(tenantId, propertyId);
        return new DailyOperationsInsight(
                snapshot.propertyId(),
                snapshot.totalOrdersToday(),
                snapshot.busiestTableId(),
                snapshot.topServerId(),
                snapshot.topServerCustomerCount(),
                snapshot.grossSalesToday()
        );
    }

    public List<StockInsight> getStockInsights(String tenantId, String propertyId) {
        return operationsInsightsProjection.stockSnapshot(tenantId, propertyId).stream()
                .map(stock -> new StockInsight(
                        stock.propertyId(),
                        stock.ingredientName(),
                        stock.stockHealth(),
                        stock.availableQuantity(),
                        stock.unit()
                ))
                .toList();
    }

    public void recordOrder(OrderCreatedEvent event) {
        operationsInsightsProjection.recordOrder(event.tenantId(), event.propertyId(), event.tableId(), event.waiterId());
    }

    public void recordSale(PaymentSucceededEvent event) {
        operationsInsightsProjection.recordSale(event.tenantId(), event.propertyId(), event.amount());
    }

    public void updateStock(StockAlertEvent event) {
        operationsInsightsProjection.updateStock(new StockHealthView(
                event.tenantId(),
                event.propertyId(),
                event.ingredientName(),
                event.stockHealth(),
                event.availableQuantity(),
                event.unit()
        ));
    }
}
