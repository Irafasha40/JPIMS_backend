package com.whizupp.jpims.controller;

import com.whizupp.jpims.dto.request.FinishedProductRequest;
import com.whizupp.jpims.dto.response.FinishedProductResponse;
import com.whizupp.jpims.entity.FinishedProduct;
import com.whizupp.jpims.enums.DomainEnums.FinishedProductStatus;
import com.whizupp.jpims.service.FinishedProductService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finished-products")
@RequiredArgsConstructor
public class FinishedProductController {
    private final FinishedProductService finishedProductService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<FinishedProductResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(finishedProductService.list(pageable));
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR','PRODUCTION_MANAGER')")
    public ResponseEntity<FinishedProductResponse> transfer(
            @Valid @RequestBody FinishedProductRequest body,
            Authentication authentication) {
        if (body.getBatchId() == null) {
            return ResponseEntity.badRequest().build();
        }
        FinishedProduct product = finishedProductService.transfer(
                body.getBatchId(),
                body.getLotNumber(),
                authentication.getName());
        return ResponseEntity.status(201).body(finishedProductService.toResponse(product));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FinishedProductResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(finishedProductService.getById(id));
    }

    @GetMapping("/{id}/movements")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<Map<String, Object>>> movements(@PathVariable UUID id, Pageable pageable) {
        return ResponseEntity.ok(Page.empty(pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> update(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(body);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable UUID id, 
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        String statusStr = (String) body.get("status");
        FinishedProductStatus newStatus = FinishedProductStatus.valueOf(statusStr);
        FinishedProduct updated = finishedProductService.updateStatus(id, newStatus, authentication.getName());
        Map<String, Object> response = Map.of(
            "id", updated.getId(),
            "status", updated.getStatus().name()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/near-expiry")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<Map<String, Object>>> nearExpiry(Pageable pageable) {
        return ResponseEntity.ok(Page.empty(pageable));
    }

    @GetMapping("/expired")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<Map<String, Object>>> expired(Pageable pageable) {
        return ResponseEntity.ok(Page.empty(pageable));
    }

    @GetMapping("/valuation")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> valuation() {
        return ResponseEntity.ok(Map.of("message", "Valuation endpoint scaffolded"));
    }

    @GetMapping("/expiry-report")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> expiryReport() {
        return ResponseEntity.ok(Map.of("message", "Expiry report endpoint scaffolded"));
    }
}
