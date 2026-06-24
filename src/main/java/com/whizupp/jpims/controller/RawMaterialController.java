package com.whizupp.jpims.controller;

import com.whizupp.jpims.entity.PurchaseOrder;
import com.whizupp.jpims.entity.RawMaterial;
import com.whizupp.jpims.entity.StockMovement;
import com.whizupp.jpims.service.PurchaseOrderService;
import com.whizupp.jpims.service.RawMaterialService;
import com.whizupp.jpims.dto.request.PurchaseOrderCreateRequest;
import com.whizupp.jpims.dto.request.PurchaseOrderReceiveRequest;
import com.whizupp.jpims.dto.response.PurchaseOrderDetailResponse;
import com.whizupp.jpims.dto.response.PurchaseOrderSummaryResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/raw-materials")
@RequiredArgsConstructor
public class RawMaterialController {
    private final RawMaterialService rawMaterialService;
    private final PurchaseOrderService purchaseOrderService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<RawMaterial>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) String stockStatus,
            Pageable pageable) {
        return ResponseEntity.ok(rawMaterialService.list(pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<RawMaterial> create(@Valid @RequestBody RawMaterial body) {
        return ResponseEntity.status(201).body(rawMaterialService.create(body));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RawMaterial> get(@PathVariable UUID id) {
        return ResponseEntity.ok(rawMaterialService.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<RawMaterial> update(@PathVariable UUID id, @Valid @RequestBody RawMaterial body) {
        return ResponseEntity.ok(rawMaterialService.update(id, body));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable UUID id) {
        rawMaterialService.softDelete(id);
        return ResponseEntity.ok(Map.of("id", id, "isActive", false));
    }

    @PostMapping("/{id}/stock-in")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> stockIn(@PathVariable UUID id, @RequestBody Map<String, Object> body, Authentication authentication) {
        Number quantity = (Number) body.getOrDefault("quantity", 0);
        String notes = String.valueOf(body.getOrDefault("notes", "Manual stock-in"));
        String expiryStr = (String) body.get("expiryDate");
        RawMaterial updated = rawMaterialService.stockIn(id, quantity.toString(), notes, authentication.getName());
        return ResponseEntity.ok(Map.of("id", id, "currentStock", updated.getCurrentStock()));
    }

    @PostMapping("/{id}/stock-out")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> stockOut(@PathVariable UUID id, @RequestBody Map<String, Object> body, Authentication authentication) {
        Number quantity = (Number) body.getOrDefault("quantity", 0);
        String notes = String.valueOf(body.getOrDefault("notes", "Manual stock-out"));
        RawMaterial updated = rawMaterialService.stockOut(id, quantity.toString(), notes, authentication.getName());
        return ResponseEntity.ok(Map.of("id", id, "currentStock", updated.getCurrentStock()));
    }

    @GetMapping("/{id}/movements")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<StockMovement>> movements(@PathVariable UUID id, Pageable pageable) {
        return ResponseEntity.ok(rawMaterialService.movements(id, pageable));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<RawMaterial>> lowStock(Pageable pageable) {
        return ResponseEntity.ok(rawMaterialService.lowStock(pageable));
    }

    @GetMapping("/expiry-alerts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<Map<String, Object>>> expiryAlerts(Pageable pageable) { 
        return ResponseEntity.ok(Page.empty(pageable)); 
    }

    @GetMapping("/purchase-orders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PurchaseOrderSummaryResponse>> purchaseOrders(Pageable pageable) {
        return ResponseEntity.ok(purchaseOrderService.list(pageable));
    }

    @PostMapping("/purchase-orders")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<PurchaseOrder> createPurchaseOrder(
            @Valid @RequestBody PurchaseOrderCreateRequest body,
            Authentication authentication) {
        return ResponseEntity.status(201).body(purchaseOrderService.create(body, authentication.getName()));
    }

    @GetMapping("/purchase-orders/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PurchaseOrderDetailResponse> getPurchaseOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrderDetail(id));
    }

    @PutMapping("/purchase-orders/{id}/receive")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<PurchaseOrder> receivePurchaseOrder(
            @PathVariable UUID id,
            @RequestBody(required = false) PurchaseOrderReceiveRequest body,
            Authentication authentication) {
        return ResponseEntity.ok(purchaseOrderService.receive(id, body, authentication.getName()));
    }

    @PutMapping("/purchase-orders/{id}/cancel")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<PurchaseOrder> cancelPurchaseOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(purchaseOrderService.cancel(id));
    }
}
