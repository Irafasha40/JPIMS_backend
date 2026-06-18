package com.whizupp.jpims.controller;

import com.whizupp.jpims.dto.response.DashboardResponse;
import com.whizupp.jpims.service.DashboardService;
import com.whizupp.jpims.service.DashboardSummaryService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;
    private final DashboardSummaryService dashboardSummaryService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }

    @GetMapping("/production-summary")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','PRODUCTION_MANAGER')")
    public ResponseEntity<Map<String, Object>> getProductionSummary() {
        return ResponseEntity.ok(dashboardSummaryService.getProductionSummary());
    }

    @GetMapping("/inventory-summary")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','INVENTORY_MANAGER')")
    public ResponseEntity<Map<String, Object>> getInventorySummary() {
        return ResponseEntity.ok(dashboardSummaryService.getInventorySummary());
    }

    @GetMapping("/sales-summary")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','SALES_STAFF')")
    public ResponseEntity<Map<String, Object>> getSalesSummary() {
        return ResponseEntity.ok(dashboardSummaryService.getSalesSummary());
    }

    @GetMapping("/production-manager")
    @PreAuthorize("hasAnyRole('PRODUCTION_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<DashboardResponse> productionManager() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }

    @GetMapping("/inventory-manager")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<DashboardResponse> inventoryManager() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }

    @GetMapping("/qc-officer")
    @PreAuthorize("hasAnyRole('QC_OFFICER','ADMINISTRATOR')")
    public ResponseEntity<DashboardResponse> qcOfficer() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }

    @GetMapping("/sales-staff")
    @PreAuthorize("hasAnyRole('SALES_STAFF','ADMINISTRATOR')")
    public ResponseEntity<DashboardResponse> salesStaff() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }

    @GetMapping("/administrator")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<DashboardResponse> administrator() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }
}
