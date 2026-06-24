package com.whizupp.jpims.service;

import com.whizupp.jpims.enums.DomainEnums.*;
import com.whizupp.jpims.repository.*;
import com.whizupp.jpims.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardSummaryService {

    private final ProductionBatchRepository batchRepository;
    private final RawMaterialRepository rawMaterialRepository;
    private final FinishedProductRepository finishedProductRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final OrderLineItemRepository orderLineItemRepository;

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 30_000L; // 30 seconds cache

    private static class CacheEntry {
        final long timestamp;
        final Map<String, Object> data;

        CacheEntry(Map<String, Object> data) {
            this.timestamp = System.currentTimeMillis();
            this.data = data;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL_MS;
        }
    }

    private Map<String, Object> getCached(String key, Supplier<Map<String, Object>> computer) {
        CacheEntry entry = cache.get(key);
        if (entry == null || entry.isExpired()) {
            Map<String, Object> freshData = computer.get();
            entry = new CacheEntry(freshData);
            cache.put(key, entry);
        }
        return entry.data;
    }

    public Map<String, Object> getProductionSummary() {
        return getCached("production-summary", () -> {
            LocalDate today = LocalDate.now();
            List<ProductionBatch> allBatches = batchRepository.findAll();

            long initiatedToday = allBatches.stream()
                    .filter(b -> today.equals(b.getProductionDate()))
                    .count();

            long inProgress = allBatches.stream()
                    .filter(b -> b.getStatus() == BatchStatus.IN_PROGRESS || b.getStatus() == BatchStatus.ISSUED)
                    .count();

            long pendingQc = allBatches.stream()
                    .filter(b -> b.getStatus() == BatchStatus.QC_PENDING)
                    .count();

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("batchesInitiatedToday", initiatedToday);
            map.put("batchesInProgress", inProgress);
            map.put("pendingQcCount", pendingQc);
            return map;
        });
    }

    public Map<String, Object> getInventorySummary() {
        return getCached("inventory-summary", () -> {
            LocalDate today = LocalDate.now();
            LocalDate plus30 = today.plusDays(30);

            long lowStockCount = rawMaterialRepository.findAll().stream()
                    .filter(rm -> rm.getCurrentStock() != null && rm.getMinimumThreshold() != null 
                            && rm.getCurrentStock().compareTo(rm.getMinimumThreshold()) <= 0)
                    .count();

            List<FinishedProduct> finishedProducts = finishedProductRepository.findAll();
            long nearExpiryCount = finishedProducts.stream()
                    .filter(fp -> fp.getExpiryDate() != null && fp.getStatus() != FinishedProductStatus.EXPIRED 
                            && fp.getStatus() != FinishedProductStatus.OUT_OF_STOCK
                            && !fp.getExpiryDate().isBefore(today) && !fp.getExpiryDate().isAfter(plus30))
                    .count();

            double totalFinishedGoodsUnits = finishedProducts.stream()
                    .mapToDouble(fp -> fp.getQuantity() != null ? fp.getQuantity().doubleValue() : 0.0)
                    .sum();

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("lowStockItemCount", lowStockCount);
            map.put("nearExpiryItemCount", nearExpiryCount);
            map.put("totalFinishedGoodsUnits", totalFinishedGoodsUnits);
            return map;
        });
    }

    public Map<String, Object> getSalesSummary() {
        return getCached("sales-summary", () -> {
            LocalDate today = LocalDate.now();
            LocalDate weekAgo = today.minusDays(7);

            List<SalesOrder> salesOrders = salesOrderRepository.findAll();

            long confirmedToday = salesOrders.stream()
                    .filter(o -> today.equals(o.getOrderDate()) && o.getStatus() == OrderStatus.CONFIRMED)
                    .count();

            long pendingFulfillment = salesOrders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.PENDING || o.getStatus() == OrderStatus.CONFIRMED)
                    .count();

            double unitsSoldThisWeek = salesOrders.stream()
                    .filter(o -> o.getOrderDate() != null && !o.getOrderDate().isBefore(weekAgo) 
                            && (o.getStatus() == OrderStatus.CONFIRMED || o.getStatus() == OrderStatus.SHIPPED || o.getStatus() == OrderStatus.DELIVERED))
                    .flatMap(o -> orderLineItemRepository.findBySalesOrderId(o.getId()).stream())
                    .mapToDouble(item -> item.getQuantity() != null ? item.getQuantity().doubleValue() : 0.0)
                    .sum();

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("ordersConfirmedToday", confirmedToday);
            map.put("ordersPendingFulfillment", pendingFulfillment);
            map.put("totalUnitsSoldThisWeek", unitsSoldThisWeek);
            return map;
        });
    }
}
