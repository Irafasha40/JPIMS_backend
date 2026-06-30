package com.whizupp.jpims.controller;

import com.whizupp.jpims.dto.response.ProductionBatchResponse;
import com.whizupp.jpims.service.BatchCompletionService;
import com.whizupp.jpims.service.BatchService;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/batches")
@RequiredArgsConstructor
public class ProductionBatchController {
    private final BatchService batchService;
    private final BatchCompletionService batchCompletionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('PRODUCTION_MANAGER','ADMINISTRATOR','QC_OFFICER','INVENTORY_MANAGER')")
    public ResponseEntity<Page<ProductionBatchResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String productName,
            Pageable pageable) {
        return ResponseEntity.ok(batchService.list(pageable, status, productName));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PRODUCTION_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(201).body(batchService.createBatch(body));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRODUCTION_MANAGER','ADMINISTRATOR','QC_OFFICER','INVENTORY_MANAGER')")
    public ResponseEntity<ProductionBatchResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(batchService.getBatch(id));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('PRODUCTION_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<ProductionBatchResponse> start(@PathVariable UUID id) {
        return ResponseEntity.ok(batchService.startProduction(id));
    }

    @PostMapping("/{id}/confirm-ingredients")
    @PreAuthorize("hasAnyRole('PRODUCTION_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<ProductionBatchResponse> confirmIngredients(@PathVariable UUID id, Authentication authentication) {
        return ResponseEntity.ok(batchService.confirmIngredients(id, authentication.getName()));
    }

    @PostMapping("/{id}/approve-stock")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<ProductionBatchResponse> approveStock(@PathVariable UUID id, Authentication authentication) {
        return ResponseEntity.ok(batchService.approveStock(id, authentication.getName()));
    }

    @PutMapping("/{id}/yield")
    @PreAuthorize("hasAnyRole('PRODUCTION_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<ProductionBatchResponse> recordYield(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(batchService.recordYield(id, body));
    }

    @PostMapping("/{id}/send-to-qc")
    @PreAuthorize("hasAnyRole('PRODUCTION_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<ProductionBatchResponse> sendToQc(@PathVariable UUID id) {
        return ResponseEntity.ok(batchService.sendToQualityControl(id));
    }

    @GetMapping("/{id}/qc-results")
    @PreAuthorize("hasAnyRole('PRODUCTION_MANAGER','ADMINISTRATOR','QC_OFFICER','INVENTORY_MANAGER')")
    public ResponseEntity<Page<Map<String, Object>>> qcResults(@PathVariable UUID id, Pageable pageable) {
        return ResponseEntity.ok(Page.empty(pageable));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('PRODUCTION_MANAGER','ADMINISTRATOR','QC_OFFICER','INVENTORY_MANAGER')")
    public ResponseEntity<Page<ProductionBatchResponse>> active(Pageable pageable) {
        return ResponseEntity.ok(batchService.list(pageable, "IN_PROGRESS", null));
    }

    @PostMapping("/sync-finished-goods")
    @PreAuthorize("hasAnyRole('PRODUCTION_MANAGER','ADMINISTRATOR','INVENTORY_MANAGER')")
    public ResponseEntity<Map<String, Object>> syncFinishedGoods(Authentication authentication) {
        int transferred = batchCompletionService.syncAllCompletedBatches(authentication.getName());
        return ResponseEntity.ok(Map.of("transferred", transferred));
    }
}
