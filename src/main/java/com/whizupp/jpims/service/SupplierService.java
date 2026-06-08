package com.whizupp.jpims.service;

import com.whizupp.jpims.entity.Supplier;
import com.whizupp.jpims.exception.InvalidOperationException;
import com.whizupp.jpims.exception.ResourceNotFoundException;
import com.whizupp.jpims.repository.SupplierRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierService {
    private final SupplierRepository supplierRepository;

    public Page<Supplier> list(Pageable pageable) {
        return supplierRepository.findAll(pageable);
    }

    public Supplier getSupplier(UUID id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
    }

    @Transactional
    public Supplier createSupplier(Supplier request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new InvalidOperationException("Supplier name is required");
        }
        return supplierRepository.save(request);
    }

    @Transactional
    public Supplier updateSupplier(UUID id, Supplier request) {
        Supplier supplier = getSupplier(id);
        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setPaymentTerms(request.getPaymentTerms());
        return supplierRepository.save(supplier);
    }

    @Transactional
    public void softDeleteSupplier(UUID id) {
        Supplier supplier = getSupplier(id);
        supplier.setIsActive(false);
        supplierRepository.save(supplier);
    }
}
