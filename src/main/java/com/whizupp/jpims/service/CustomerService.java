package com.whizupp.jpims.service;

import com.whizupp.jpims.entity.Customer;
import com.whizupp.jpims.exception.InvalidOperationException;
import com.whizupp.jpims.exception.ResourceNotFoundException;
import com.whizupp.jpims.repository.CustomerRepository;
import com.whizupp.jpims.repository.SalesOrderRepository;
import java.time.LocalDate;
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
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final SalesOrderRepository salesOrderRepository;

    public Page<Customer> list(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    public Customer getCustomer(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    @Transactional
    public Customer createCustomer(Customer request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new InvalidOperationException("Customer name is required");
        }
        return customerRepository.save(request);
    }

    @Transactional
    public Customer updateCustomer(UUID id, Customer request) {
        Customer customer = getCustomer(id);
        customer.setName(request.getName());
        customer.setContactPerson(request.getContactPerson());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());
        return customerRepository.save(customer);
    }

    @Transactional
    public void softDeleteCustomer(UUID id) {
        Customer customer = getCustomer(id);
        customer.setIsActive(false);
        customerRepository.save(customer);
    }
}
