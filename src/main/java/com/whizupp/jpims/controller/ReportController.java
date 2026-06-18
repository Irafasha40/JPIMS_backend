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

    @GetMapping("/production")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'PRODUCTION_MANAGER')")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> production(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) UUID recipeId,
            @RequestParam(required = false) String export) {
        Map<String, Object> report = reportService.getProductionReport(from, to, recipeId);
        if ("pdf".equalsIgnoreCase(export)) {
            byte[] pdfBytes = ReportGenerator.generatePdf(
                    "Production Report",
                    (Map<String, Object>) report.get("summary"),
                    (List<Map<String, Object>>) report.get("data"),
                    from,
                    to
            );
            return fileResponse(pdfBytes, "production_report.pdf", "application/pdf");
        } else if ("excel".equalsIgnoreCase(export)) {
            byte[] excelBytes = ReportGenerator.generateExcel(
                    "Production Report",
                    (Map<String, Object>) report.get("summary"),
                    (List<Map<String, Object>>) report.get("data")
            );
            return fileResponse(excelBytes, "production_report.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }
        return ResponseEntity.ok(report);
    }

    @GetMapping("/quality")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'PRODUCTION_MANAGER', 'QC_OFFICER')")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> quality(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) String export) {
        Map<String, Object> report = reportService.getQualityReport(from, to, productId);
        if ("pdf".equalsIgnoreCase(export)) {
            byte[] pdfBytes = ReportGenerator.generatePdf(
                    "Quality Performance Report",
                    (Map<String, Object>) report.get("summary"),
                    (List<Map<String, Object>>) report.get("data"),
                    from,
                    to
            );
            return fileResponse(pdfBytes, "quality_report.pdf", "application/pdf");
        } else if ("excel".equalsIgnoreCase(export)) {
            byte[] excelBytes = ReportGenerator.generateExcel(
                    "Quality Performance Report",
                    (Map<String, Object>) report.get("summary"),
                    (List<Map<String, Object>>) report.get("data")
            );
            return fileResponse(excelBytes, "quality_report.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }
        return ResponseEntity.ok(report);
    }

    @GetMapping("/inventory")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'PRODUCTION_MANAGER', 'INVENTORY_MANAGER')")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> inventory(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String export) {
        Map<String, Object> report = reportService.getInventoryReport(from, to);
        if ("pdf".equalsIgnoreCase(export)) {
            byte[] pdfBytes = ReportGenerator.generatePdf(
                    "Inventory Report",
                    (Map<String, Object>) report.get("summary"),
                    (List<Map<String, Object>>) report.get("data"),
                    from,
                    to
            );
            return fileResponse(pdfBytes, "inventory_report.pdf", "application/pdf");
        } else if ("excel".equalsIgnoreCase(export)) {
            byte[] excelBytes = ReportGenerator.generateExcel(
                    "Inventory Report",
                    (Map<String, Object>) report.get("summary"),
                    (List<Map<String, Object>>) report.get("data")
            );
            return fileResponse(excelBytes, "inventory_report.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }
        return ResponseEntity.ok(report);
    }

    @GetMapping("/sales")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'PRODUCTION_MANAGER', 'SALES_STAFF')")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> sales(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) String customer,
            @RequestParam(required = false) String export) {
        Map<String, Object> report = reportService.getSalesReport(from, to, productId, customer);
        if ("pdf".equalsIgnoreCase(export)) {
            byte[] pdfBytes = ReportGenerator.generatePdf(
                    "Sales Report",
                    (Map<String, Object>) report.get("summary"),
                    (List<Map<String, Object>>) report.get("data"),
                    from,
                    to
            );
            return fileResponse(pdfBytes, "sales_report.pdf", "application/pdf");
        } else if ("excel".equalsIgnoreCase(export)) {
            byte[] excelBytes = ReportGenerator.generateExcel(
                    "Sales Report",
                    (Map<String, Object>) report.get("summary"),
                    (List<Map<String, Object>>) report.get("data")
            );
            return fileResponse(excelBytes, "sales_report.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
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
        if ("pdf".equalsIgnoreCase(export)) {
            byte[] pdfBytes = ReportGenerator.generatePdf(
                    "Wastage Report",
                    (Map<String, Object>) report.get("summary"),
                    (List<Map<String, Object>>) report.get("data"),
                    from,
                    to
            );
            return fileResponse(pdfBytes, "wastage_report.pdf", "application/pdf");
        } else if ("excel".equalsIgnoreCase(export)) {
            byte[] excelBytes = ReportGenerator.generateExcel(
                    "Wastage Report",
                    (Map<String, Object>) report.get("summary"),
                    (List<Map<String, Object>>) report.get("data")
            );
            return fileResponse(excelBytes, "wastage_report.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }
        return ResponseEntity.ok(report);
    }

    @GetMapping("/scheduled")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','PRODUCTION_MANAGER','INVENTORY_MANAGER')")
    public ResponseEntity<Page<Map<String, Object>>> scheduled(Pageable pageable) {
        return ResponseEntity.ok(reportService.getScheduledReports(pageable));
    }

    @PostMapping("/scheduled")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','PRODUCTION_MANAGER','INVENTORY_MANAGER')")
    public ResponseEntity<Map<String, Object>> createScheduled(@RequestBody Map<String, Object> body, Authentication authentication) {
        return ResponseEntity.status(201).body(reportService.createScheduledReport(body, authentication.getName()));
    }

    @PutMapping("/scheduled/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','PRODUCTION_MANAGER','INVENTORY_MANAGER')")
    public ResponseEntity<Map<String, Object>> updateScheduled(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(reportService.updateScheduledReport(id, body));
    }

    @DeleteMapping("/scheduled/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','PRODUCTION_MANAGER','INVENTORY_MANAGER')")
    public ResponseEntity<Map<String, Object>> deleteScheduled(@PathVariable UUID id) {
        reportService.deleteScheduledReport(id);
        return ResponseEntity.ok(Map.of("id", id));
    }

    private ResponseEntity<byte[]> fileResponse(byte[] bytes, String fileName, String contentType) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(bytes);
    }
}
