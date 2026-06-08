package com.whizupp.jpims.controller;

import com.whizupp.jpims.dto.request.CustomerRequest;
import com.whizupp.jpims.dto.response.CustomerResponse;
import com.whizupp.jpims.entity.Customer;
import com.whizupp.jpims.service.CustomerService;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers")
@PreAuthorize("hasAnyRole('SALES_STAFF','ADMINISTRATOR')")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> list(Pageable pageable) {
        Page<Customer> customers = customerService.list(pageable);
        return ResponseEntity.ok(customers.map(this::mapToResponse));
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest body) {
        Customer customer = Customer.builder()
                .name(body.getName())
                .contactPerson(body.getContact())
                .phone(body.getPhone())
                .email(body.getEmail())
                .address(body.getAddress())
                .build();
        return ResponseEntity.status(201).body(mapToResponse(customerService.createCustomer(customer)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(mapToResponse(customerService.getCustomer(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(@PathVariable UUID id, @Valid @RequestBody CustomerRequest body) {
        Customer customer = Customer.builder()
                .name(body.getName())
                .contactPerson(body.getContact())
                .phone(body.getPhone())
                .email(body.getEmail())
                .address(body.getAddress())
                .build();
        return ResponseEntity.ok(mapToResponse(customerService.updateCustomer(id, customer)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable UUID id) {
        customerService.softDeleteCustomer(id);
        return ResponseEntity.ok(Map.of("id", id, "isActive", false));
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<Page<Map<String, Object>>> orders(@PathVariable UUID id, Pageable pageable) {
        return ResponseEntity.ok(Page.empty(pageable));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Object>> stats(@PathVariable UUID id) {
        return ResponseEntity.ok(Map.of("id", id));
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .contact(customer.getContactPerson())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .build();
    }
}
