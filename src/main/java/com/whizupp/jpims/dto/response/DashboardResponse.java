package com.whizupp.jpims.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private KPIs kpis;
    private List<Map<String, Object>> recentActivity;
    private Map<String, Object> charts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KPIs {
        private Long totalProduction;
        private Long totalQCTests;
        private Long totalSales;
        private BigDecimal inventoryValue;
        private BigDecimal lowStockItems;
        private Double qualityPassRate;
        private Long pendingBatches;
    }
}
