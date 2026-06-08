package com.whizupp.jpims.service;

import com.whizupp.jpims.dto.response.DashboardResponse;
import com.whizupp.jpims.enums.DomainEnums.BatchStatus;
import com.whizupp.jpims.enums.DomainEnums.OrderStatus;
import com.whizupp.jpims.enums.DomainEnums.TestResult;
import com.whizupp.jpims.repository.ProductionBatchRepository;
import com.whizupp.jpims.repository.QualityTestRepository;
import com.whizupp.jpims.repository.RawMaterialRepository;
import com.whizupp.jpims.repository.SalesOrderRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final ProductionBatchRepository batchRepository;
    private final QualityTestRepository qualityTestRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final RawMaterialRepository rawMaterialRepository;

    public DashboardResponse getDashboard() {
        DashboardResponse.KPIs kpis = DashboardResponse.KPIs.builder()
                .totalProduction(batchRepository.count())
                .totalQCTests(qualityTestRepository.count())
                .totalSales(salesOrderRepository.count())
                .inventoryValue(calculateInventoryValue())
                .lowStockItems(countLowStockItems())
                .qualityPassRate(calculateQualityPassRate())
                .pendingBatches(countPendingBatches())
                .build();

        List<Map<String, Object>> recentActivity = getRecentActivity();
        Map<String, Object> charts = getChartData();

        return DashboardResponse.builder()
                .kpis(kpis)
                .recentActivity(recentActivity)
                .charts(charts)
                .build();
    }

    private BigDecimal calculateInventoryValue() {
        return rawMaterialRepository.findAll().stream()
                .map(rm -> rm.getCurrentStock().multiply(rm.getCostPerUnit()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal countLowStockItems() {
        long count = rawMaterialRepository.findAll().stream()
                .filter(rm -> rm.getCurrentStock().compareTo(rm.getMinimumThreshold()) <= 0)
                .count();
        return BigDecimal.valueOf(count);
    }

    private Double calculateQualityPassRate() {
        long totalTests = qualityTestRepository.count();
        if (totalTests == 0) return 0.0;

        long passTests = qualityTestRepository.countByResult(TestResult.PASS);
        return (double) passTests / totalTests * 100;
    }

    private Long countPendingBatches() {
        return batchRepository.countByStatus(BatchStatus.PLANNED);
    }

    private List<Map<String, Object>> getRecentActivity() {
        List<Map<String, Object>> activity = new ArrayList<>();

        batchRepository.findAll().stream().limit(5).forEach(batch -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", "BATCH");
            item.put("title", "Batch " + batch.getBatchNumber());
            item.put("description", "Status: " + batch.getStatus());
            item.put("timestamp", batch.getCreatedAt());
            activity.add(item);
        });

        return activity;
    }

    private Map<String, Object> getChartData() {
        Map<String, Object> charts = new HashMap<>();

        // Batch status distribution
        Map<String, Long> batchStatus = new HashMap<>();
        for (BatchStatus status : BatchStatus.values()) {
            batchStatus.put(status.name(), batchRepository.countByStatus(status));
        }
        charts.put("batchStatus", batchStatus);

        // Order status distribution
        Map<String, Long> orderStatus = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            orderStatus.put(status.name(), salesOrderRepository.countByStatus(status));
        }
        charts.put("orderStatus", orderStatus);

        return charts;
    }
}
