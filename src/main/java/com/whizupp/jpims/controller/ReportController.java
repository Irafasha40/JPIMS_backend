package com.whizupp.jpims.controller;

import com.whizupp.jpims.service.ReportService;
import com.whizupp.jpims.util.ReportGenerator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final com.whizupp.jpims.repository.UserRepository userRepository;

    @GetMapping("/production")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'PRODUCTION_MANAGER')")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> production(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) UUID recipeId,
            @RequestParam(required = false) String export) {
        Map<String, Object> report = reportService.getProductionReport(from, to, recipeId);
        String generatedBy = getCreatedBy();
        if ("pdf".equalsIgnoreCase(export)) {
            byte[] pdfBytes = ReportGenerator.generatePdf(
                    "Production Report",
                    (Map<String, Object>) report.get("summary"),
                    (List<Map<String, Object>>) report.get("data"),
                    from,
                    to,
                    generatedBy
            );
            return fileResponse(pdfBytes, "production_report.pdf", "application/pdf");
        } else if ("excel".equalsIgnoreCase(export)) {
            byte[] excelBytes = ReportGenerator.generateExcel(
                    "Production Report",
                    (Map<String, Object>) report.get("summary"),
                    (List<Map<String, Object>>) report.get("data"),
                    generatedBy
            );
            return fileResponse(excelBytes, "production_report.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }
        return ResponseEntity.ok(report);
    }

    @GetMapping("/quality")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'QC_OFFICER')")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> quality(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) String export) {
        Map<String, Object> report = reportService.getQualityReport(from, to, productId);
        String generatedBy = getCreatedBy();
        if ("pdf".equalsIgnoreCase(export)) {
            byte[] pdfBytes = ReportGenerator.generatePdf(
                    "Quality Performance Report",
                    (Map<String, Object>) report.get("summary"),
                    (List<Map<String, Object>>) report.get("data"),
                    from,
                    to,
                    generatedBy
            );
            return fileResponse(pdfBytes, "quality_report.pdf", "application/pdf");
        } else if ("excel".equalsIgnoreCase(export)) {
            byte[] excelBytes = ReportGenerator.generateExcel(
                    "Quality Performance Report",
                    (Map<String, Object>) report.get("summary"),
                    (List<Map<String, Object>>) report.get("data"),
                    generatedBy
            );
            return fileResponse(excelBytes, "quality_report.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }
        return ResponseEntity.ok(report);
    }

    @GetMapping("/inventory")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INVENTORY_MANAGER')")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> inventory(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String export) {
        Map<String, Object> report = reportService.getInventoryReport(from, to, type);
        String generatedBy = getCreatedBy();
        if ("pdf".equalsIgnoreCase(export)) {
            byte[] pdfBytes = ReportGenerator.generatePdf(
                    "Inventory Report",
                    (Map<String, Object>) report.get("summary"),
                    (List<Map<String, Object>>) report.get("data"),
                    from,
                    to,
                    generatedBy
            );
            return fileResponse(pdfBytes, "inventory_report.pdf", "application/pdf");
        } else if ("excel".equalsIgnoreCase(export)) {
            byte[] excelBytes = ReportGenerator.generateExcel(
                    "Inventory Report",
                    (Map<String, Object>) report.get("summary"),
                    (List<Map<String, Object>>) report.get("data"),
                    generatedBy
            );
            return fileResponse(excelBytes, "inventory_report.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }
        return ResponseEntity.ok(report);
    }

    @GetMapping("/sales")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SALES_STAFF')")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> sales(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) String customer,
            @RequestParam(required = false) String export) {
        Map<String, Object> report = reportService.getSalesReport(from, to, productId, customer);
        String generatedBy = getCreatedBy();
        if ("pdf".equalsIgnoreCase(export)) {
            byte[] pdfBytes = ReportGenerator.generatePdf(
                    "Sales Report",
                    (Map<String, Object>) report.get("summary"),
                    (List<Map<String, Object>>) report.get("data"),
                    from,
                    to,
                    generatedBy
            );
            return fileResponse(pdfBytes, "sales_report.pdf", "application/pdf");
        } else if ("excel".equalsIgnoreCase(export)) {
            byte[] excelBytes = ReportGenerator.generateExcel(
                    "Sales Report",
                    (Map<String, Object>) report.get("summary"),
                    (List<Map<String, Object>>) report.get("data"),
                    generatedBy
            );
            return fileResponse(excelBytes, "sales_report.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }
        return ResponseEntity.ok(report);
    }

    @GetMapping("/wastage")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'PRODUCTION_MANAGER', 'INVENTORY_MANAGER')")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> wastage(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String export) {
        Map<String, Object> report = reportService.getWastageReport(from, to);
        String generatedBy = getCreatedBy();
        if ("pdf".equalsIgnoreCase(export)) {
            byte[] pdfBytes = ReportGenerator.generatePdf(
                    "Wastage Report",
                    (Map<String, Object>) report.get("summary"),
                    (List<Map<String, Object>>) report.get("data"),
                    from,
                    to,
                    generatedBy
            );
            return fileResponse(pdfBytes, "wastage_report.pdf", "application/pdf");
        } else if ("excel".equalsIgnoreCase(export)) {
            byte[] excelBytes = ReportGenerator.generateExcel(
                    "Wastage Report",
                    (Map<String, Object>) report.get("summary"),
                    (List<Map<String, Object>>) report.get("data"),
                    generatedBy
            );
            return fileResponse(excelBytes, "wastage_report.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }
        return ResponseEntity.ok(report);
    }

    @GetMapping("/stock-movements")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','INVENTORY_MANAGER')")
    public ResponseEntity<?> stockMovements(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String materialId,
            @RequestParam(required = false) String export) {
        Map<String, Object> report = reportService.getStockMovementsReport(from, to, materialId);
        String generatedBy = getCreatedBy();
        if ("pdf".equalsIgnoreCase(export)) {
            byte[] pdfBytes = ReportGenerator.generatePdf(
                    "Stock Movements Report",
                    (Map<String, Object>) report.get("summary"),
                    (List<Map<String, Object>>) report.get("data"),
                    from,
                    to,
                    generatedBy
            );
            return fileResponse(pdfBytes, "stock_movements_report.pdf", "application/pdf");
        } else if ("excel".equalsIgnoreCase(export)) {
            byte[] excelBytes = ReportGenerator.generateExcel(
                    "Stock Movements Report",
                    (Map<String, Object>) report.get("summary"),
                    (List<Map<String, Object>>) report.get("data"),
                    generatedBy
            );
            return fileResponse(excelBytes, "stock_movements_report.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }
        return ResponseEntity.ok(report);
    }

    @GetMapping("/scheduled")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','INVENTORY_MANAGER')")
    public ResponseEntity<Page<Map<String, Object>>> scheduled(Pageable pageable) {
        return ResponseEntity.ok(reportService.getScheduledReports(pageable));
    }

    @PostMapping("/scheduled")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','INVENTORY_MANAGER')")
    public ResponseEntity<Map<String, Object>> createScheduled(@RequestBody Map<String, Object> body,
            Authentication authentication) {
        return ResponseEntity.status(201).body(reportService.createScheduledReport(body, authentication.getName()));
    }

    @PutMapping("/scheduled/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','INVENTORY_MANAGER')")
    public ResponseEntity<Map<String, Object>> updateScheduled(@PathVariable UUID id,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(reportService.updateScheduledReport(id, body));
    }

    @DeleteMapping("/scheduled/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','INVENTORY_MANAGER')")
    public ResponseEntity<Map<String, Object>> deleteScheduled(@PathVariable UUID id) {
        reportService.deleteScheduledReport(id);
        return ResponseEntity.ok(Map.of("id", id));
    }

    private String getCreatedBy() {
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
                String email = auth.getName();
                return userRepository.findByEmail(email)
                        .map(com.whizupp.jpims.entity.User::getFullName)
                        .orElse(email);
            }
        } catch (Exception e) {
            // fallback
        }
        return "System";
    }

    private ResponseEntity<byte[]> fileResponse(byte[] bytes, String fileName, String contentType) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(bytes);
    }
}
