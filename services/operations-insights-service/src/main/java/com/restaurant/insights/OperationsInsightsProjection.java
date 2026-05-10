package com.restaurant.insights;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OperationsInsightsProjection {

    private final Map<String, Integer> ordersByScope = new ConcurrentHashMap<>();
    private final Map<String, Integer> tableCounts = new ConcurrentHashMap<>();
    private final Map<String, Integer> waiterCounts = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> grossSalesByScope = new ConcurrentHashMap<>();
    private final Map<String, StockHealthView> stockByScopeAndIngredient = new ConcurrentHashMap<>();

    public void recordOrder(String tenantId, String propertyId, String tableId, String waiterId) {
        String scope = scopeKey(tenantId, propertyId);
        ordersByScope.merge(scope, 1, Integer::sum);
        tableCounts.merge(scope + "::" + tableId, 1, Integer::sum);
        waiterCounts.merge(scope + "::" + waiterId, 1, Integer::sum);
    }

    public void recordSale(String tenantId, String propertyId, BigDecimal amount) {
        grossSalesByScope.merge(scopeKey(tenantId, propertyId), amount, BigDecimal::add);
    }

    public void updateStock(StockHealthView stockHealthView) {
        stockByScopeAndIngredient.put(scopeKey(stockHealthView.tenantId(), stockHealthView.propertyId()) + "::" + stockHealthView.ingredientName(), stockHealthView);
    }

    public DailySnapshot snapshot(String tenantId, String propertyId) {
        String scope = scopeKey(tenantId, propertyId);
        String busiestTable = tableCounts.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(scope + "::"))
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(entry -> entry.getKey().substring(entry.getKey().indexOf("::") + 2))
                .orElse("N/A");
        Map.Entry<String, Integer> bestWaiter = waiterCounts.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(scope + "::"))
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .orElse(null);
        String topServerId = bestWaiter != null ? bestWaiter.getKey().substring(bestWaiter.getKey().indexOf("::") + 2) : "N/A";
        int topServerCustomerCount = bestWaiter != null ? bestWaiter.getValue() : 0;
        return new DailySnapshot(
                tenantId,
                propertyId,
                ordersByScope.getOrDefault(scope, 0),
                busiestTable,
                topServerId,
                topServerCustomerCount,
                grossSalesByScope.getOrDefault(scope, BigDecimal.ZERO)
        );
    }

    public List<StockHealthView> stockSnapshot(String tenantId, String propertyId) {
        return stockByScopeAndIngredient.values().stream()
                .filter(stock -> tenantId.equals(stock.tenantId()) && propertyId.equals(stock.propertyId()))
                .sorted(Comparator.comparing(StockHealthView::ingredientName))
                .toList();
    }

    private String scopeKey(String tenantId, String propertyId) {
        return tenantId + "::" + propertyId;
    }
}
