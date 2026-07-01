package com.whizupp.jpims.controller;

import com.whizupp.jpims.service.ProductCatalogService;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product-catalog")
@RequiredArgsConstructor
public class ProductCatalogController {

    private final ProductCatalogService productCatalogService;

    @GetMapping
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<Page<Map<String, Object>>> list(Pageable pageable) {
        return ResponseEntity.ok(productCatalogService.list(pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(201).body(productCatalogService.create(body));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(productCatalogService.update(id, body));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable UUID id) {
        productCatalogService.delete(id);
        return ResponseEntity.ok(Map.of("id", id, "deleted", true));
    }

    @PostMapping("/sync-prices")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> syncPrices() {
        return ResponseEntity.ok(productCatalogService.syncPricesToFinishedProducts());
    }
}
