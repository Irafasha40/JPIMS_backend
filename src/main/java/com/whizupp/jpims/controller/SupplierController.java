package com.whizupp.jpims.controller;

import com.whizupp.jpims.dto.request.SupplierRequest;
import com.whizupp.jpims.dto.response.SupplierResponse;
import com.whizupp.jpims.entity.Supplier;
import com.whizupp.jpims.service.SupplierService;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {
    private final SupplierService supplierService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<SupplierResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String onboardingStatus,
            Pageable pageable) {
        Page<Supplier> suppliers = supplierService.list(pageable);
        return ResponseEntity.ok(suppliers.map(this::mapToResponse));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierRequest body) {
        Supplier supplier = Supplier.builder()
                .name(body.getName())
                .contactPerson(body.getContact())
                .phone(body.getPhone())
                .email(body.getEmail())
                .address(body.getAddress())
                .paymentTerms(body.getPaymentTerms())
                .build();
        return ResponseEntity.status(201).body(mapToResponse(supplierService.createSupplier(supplier)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<SupplierResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(mapToResponse(supplierService.getSupplier(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<SupplierResponse> update(@PathVariable UUID id, @Valid @RequestBody SupplierRequest body) {
        Supplier supplier = Supplier.builder()
                .name(body.getName())
                .contactPerson(body.getContact())
                .phone(body.getPhone())
                .email(body.getEmail())
                .address(body.getAddress())
                .paymentTerms(body.getPaymentTerms())
                .build();
        return ResponseEntity.ok(mapToResponse(supplierService.updateSupplier(id, supplier)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable UUID id) {
        supplierService.softDeleteSupplier(id);
        return ResponseEntity.ok(Map.of("id", id, "isActive", false));
    }

    @GetMapping("/{id}/materials")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<Page<Map<String, Object>>> materials(@PathVariable UUID id, Pageable pageable) { return ResponseEntity.ok(Page.empty(pageable)); }

    @GetMapping("/{id}/purchase-orders")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<Page<Map<String, Object>>> purchaseOrders(@PathVariable UUID id, Pageable pageable) { return ResponseEntity.ok(Page.empty(pageable)); }

    @PostMapping("/{id}/rate")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> rate(@PathVariable UUID id, @RequestBody Map<String, Object> body) { return ResponseEntity.ok(body); }

    @PutMapping("/{id}/onboard")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> onboard(@PathVariable UUID id) { return ResponseEntity.ok(Map.of("id", id, "status", "ACTIVE")); }

    @GetMapping("/{id}/communications")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<Page<Map<String, Object>>> communications(@PathVariable UUID id, Pageable pageable) { return ResponseEntity.ok(Page.empty(pageable)); }

    @PostMapping("/{id}/communications")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> addCommunication(@PathVariable UUID id, @RequestBody Map<String, Object> body) { return ResponseEntity.status(201).body(body); }

    @PutMapping("/{id}/communications/{commId}")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> updateCommunication(@PathVariable UUID id, @PathVariable UUID commId, @RequestBody Map<String, Object> body) { return ResponseEntity.ok(body); }

    @GetMapping("/{id}/documents")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<Page<Map<String, Object>>> documents(@PathVariable UUID id, Pageable pageable) { return ResponseEntity.ok(Page.empty(pageable)); }

    @PostMapping("/{id}/documents")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> uploadDoc(@PathVariable UUID id, @RequestBody Map<String, Object> body) { return ResponseEntity.status(201).body(body); }

    @DeleteMapping("/{id}/documents/{docId}")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> deleteDoc(@PathVariable UUID id, @PathVariable UUID docId) { return ResponseEntity.ok(Map.of("docId", docId)); }

    @GetMapping("/performance")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> performance() { return ResponseEntity.ok(Map.of("message", "Supplier performance endpoint scaffolded")); }

    private SupplierResponse mapToResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .contact(supplier.getContactPerson())
                .phone(supplier.getPhone())
                .email(supplier.getEmail())
                .address(supplier.getAddress())
                .paymentTerms(supplier.getPaymentTerms())
                .status(supplier.getIsActive() ? "ACTIVE" : "INACTIVE")
                .build();
    }
}
