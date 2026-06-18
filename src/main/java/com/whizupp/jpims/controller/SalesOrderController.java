package com.whizupp.jpims.controller;

import com.whizupp.jpims.service.SalesOrderService;
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
@RequestMapping("/api/orders")
@PreAuthorize("hasAnyRole('SALES_STAFF','ADMINISTRATOR')")
@RequiredArgsConstructor
public class SalesOrderController {
    private final SalesOrderService salesOrderService;
    @GetMapping
    public ResponseEntity<Page<Map<String, Object>>> list(Pageable pageable) { return ResponseEntity.ok(salesOrderService.list(pageable)); }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body, Authentication authentication) {
        return ResponseEntity.status(201).body(salesOrderService.create(body, authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable UUID id) { return ResponseEntity.ok(Map.of("id", id)); }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<Map<String, Object>> confirm(@PathVariable UUID id, Authentication authentication) {
        int affectedItems = salesOrderService.confirm(id, authentication.getName());
        return ResponseEntity.ok(Map.of("id", id, "status", "CONFIRMED", "affectedItems", affectedItems));
    }

    @PutMapping("/{id}/ship")
    public ResponseEntity<Map<String, Object>> ship(@PathVariable UUID id) {
        salesOrderService.ship(id);
        return ResponseEntity.ok(Map.of("id", id, "status", "SHIPPED"));
    }

    @PutMapping("/{id}/deliver")
    public ResponseEntity<Map<String, Object>> deliver(@PathVariable UUID id) {
        salesOrderService.deliver(id);
        return ResponseEntity.ok(Map.of("id", id, "status", "DELIVERED"));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancel(@PathVariable UUID id, Authentication authentication) {
        int affectedItems = salesOrderService.cancel(id, authentication.getName());
        return ResponseEntity.ok(Map.of("id", id, "status", "CANCELLED", "affectedItems", affectedItems));
    }

    @GetMapping("/{id}/invoice")
    public ResponseEntity<Map<String, Object>> invoice(@PathVariable UUID id) {
        return ResponseEntity.ok(salesOrderService.getInvoice(id));
    }
}
