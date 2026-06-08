package com.whizupp.jpims.controller;

import com.whizupp.jpims.dto.request.QualityTestRequest;
import com.whizupp.jpims.dto.response.ProductionBatchResponse;
import com.whizupp.jpims.dto.response.QualityTestResponse;
import com.whizupp.jpims.entity.QualityTest;
import com.whizupp.jpims.service.BatchService;
import com.whizupp.jpims.service.QualityService;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/quality")
@RequiredArgsConstructor
public class QualityController {
    private final QualityService qualityService;
    private final BatchService batchService;

    @GetMapping
    @PreAuthorize("hasAnyRole('QC_OFFICER','ADMINISTRATOR','PRODUCTION_MANAGER')")
    public ResponseEntity<Page<QualityTestResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(qualityService.list(pageable));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('QC_OFFICER','ADMINISTRATOR','PRODUCTION_MANAGER')")
    public ResponseEntity<Page<ProductionBatchResponse>> pending(Pageable pageable) {
        return ResponseEntity.ok(batchService.list(pageable, "QC_PENDING", null));
    }

    @GetMapping("/trends")
    @PreAuthorize("hasAnyRole('QC_OFFICER','ADMINISTRATOR','PRODUCTION_MANAGER')")
    public ResponseEntity<Map<String, Object>> trends() {
        return ResponseEntity.ok(Map.of("message", "QC trends endpoint scaffolded"));
    }

    @GetMapping("/thresholds")
    @PreAuthorize("hasAnyRole('QC_OFFICER','ADMINISTRATOR')")
    public ResponseEntity<Page<Map<String, Object>>> thresholds(Pageable pageable) {
        return ResponseEntity.ok(Page.empty(pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('QC_OFFICER','ADMINISTRATOR')")
    public ResponseEntity<QualityTestResponse> create(
            @Valid @RequestBody QualityTestRequest body,
            Authentication authentication) {
        QualityTest saved = qualityService.create(body, authentication.getName());
        return ResponseEntity.status(201).body(qualityService.toResponse(saved));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('QC_OFFICER','ADMINISTRATOR','PRODUCTION_MANAGER')")
    public ResponseEntity<QualityTestResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(qualityService.getById(id));
    }

    @GetMapping("/batch/{batchId}/certificate")
    @PreAuthorize("hasAnyRole('QC_OFFICER','ADMINISTRATOR','PRODUCTION_MANAGER')")
    public ResponseEntity<Map<String, Object>> certificate(@PathVariable UUID batchId) {
        return ResponseEntity.ok(Map.of("batchId", batchId));
    }

    @PostMapping("/thresholds")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> createThreshold(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(201).body(body);
    }

    @PutMapping("/thresholds/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> updateThreshold(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/thresholds/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> deleteThreshold(@PathVariable UUID id) {
        return ResponseEntity.ok(Map.of("id", id));
    }
}
