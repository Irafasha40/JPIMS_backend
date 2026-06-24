package com.whizupp.jpims.service;

import com.whizupp.jpims.entity.Customer;
import com.whizupp.jpims.entity.FinishedProduct;
import com.whizupp.jpims.entity.FinishedProductMovement;
import com.whizupp.jpims.entity.OrderLineItem;
import com.whizupp.jpims.entity.SalesOrder;
import com.whizupp.jpims.entity.User;
import com.whizupp.jpims.enums.DomainEnums.FinishedProductMovementType;
import com.whizupp.jpims.enums.DomainEnums.FinishedProductStatus;
import com.whizupp.jpims.enums.DomainEnums.OrderStatus;
import com.whizupp.jpims.enums.DomainEnums.PaymentMethod;
import com.whizupp.jpims.exception.InsufficientStockException;
import com.whizupp.jpims.exception.InvalidOperationException;
import com.whizupp.jpims.exception.ResourceNotFoundException;
import com.whizupp.jpims.repository.CustomerRepository;
import com.whizupp.jpims.repository.FinishedProductMovementRepository;
import com.whizupp.jpims.repository.FinishedProductRepository;
import com.whizupp.jpims.repository.OrderLineItemRepository;
import com.whizupp.jpims.repository.SalesOrderRepository;
import com.whizupp.jpims.repository.UserRepository;
import com.whizupp.jpims.util.OrderNumberGenerator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesOrderService {
    private final SalesOrderRepository salesOrderRepository;
    private final OrderLineItemRepository lineItemRepository;
    private final FinishedProductRepository finishedProductRepository;
    private final FinishedProductMovementRepository finishedProductMovementRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final OrderNumberGenerator orderNumberGenerator;

    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> create(Map<String, Object> request, String actorEmail) {
        String customerIdRaw = String.valueOf(request.get("customerId"));
        String paymentMethodRaw = String.valueOf(request.getOrDefault("paymentMethod", "OTHER"));
        List<Map<String, Object>> lines = (List<Map<String, Object>>) request.get("lineItems");

        if (customerIdRaw == null || "null".equals(customerIdRaw)) {
            throw new InvalidOperationException("customerId is required");
        }
        if (lines == null || lines.isEmpty()) {
            throw new InvalidOperationException("At least one line item is required");
        }

        Customer customer = customerRepository.findById(UUID.fromString(customerIdRaw))
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Actor user not found"));

        List<String> failures = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        List<OrderLineItem> pendingLineItems = new ArrayList<>();

        SalesOrder order = salesOrderRepository.save(SalesOrder.builder()
                .orderNumber(orderNumberGenerator.generate())
                .customer(customer)
                .createdByUser(actor)
                .orderDate(LocalDate.now())
                .paymentMethod(PaymentMethod.valueOf(paymentMethodRaw))
                .status(OrderStatus.PENDING)
                .notes((String) request.get("notes"))
                .totalAmount(BigDecimal.ZERO)
                .build());

        for (Map<String, Object> line : lines) {
            UUID finishedProductId = UUID.fromString(String.valueOf(line.get("finishedProductId")));
            BigDecimal quantity = new BigDecimal(String.valueOf(line.get("quantity")));

            FinishedProduct product = finishedProductRepository.findById(finishedProductId)
                    .orElseThrow(() -> new ResourceNotFoundException("Finished product not found"));

            if (product.getStatus() != FinishedProductStatus.AVAILABLE || product.getQuantity().compareTo(quantity) < 0) {
                failures.add(product.getProductName() + " (required: " + quantity + ", available: " + product.getQuantity() + ")");
                continue;
            }

            BigDecimal unitPrice = line.get("unitPrice") == null
                    ? (product.getUnitCost() == null ? BigDecimal.ZERO : product.getUnitCost())
                    : new BigDecimal(String.valueOf(line.get("unitPrice")));
            BigDecimal lineTotal = unitPrice.multiply(quantity);
            total = total.add(lineTotal);

            pendingLineItems.add(OrderLineItem.builder()
                    .salesOrder(order)
                    .finishedProduct(product)
                    .quantity(quantity)
                    .unitPrice(unitPrice)
                    .lineTotal(lineTotal)
                    .build());
        }

        if (!failures.isEmpty()) {
            throw new InvalidOperationException("Insufficient or unavailable stock for: " + String.join(", ", failures));
        }

        lineItemRepository.saveAll(pendingLineItems);
        order.setTotalAmount(total);
        salesOrderRepository.save(order);
        notificationService.notifyNewOrder(order);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", order.getId());
        response.put("orderNumber", order.getOrderNumber());
        response.put("status", order.getStatus());
        response.put("totalAmount", order.getTotalAmount());
        response.put("lineItems", pendingLineItems.size());
        return response;
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> list(Pageable pageable) {
        Page<SalesOrder> orders = salesOrderRepository.findAll(pageable);
        return orders.map(order -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", order.getId());
            map.put("orderNumber", order.getOrderNumber());
            map.put("orderDate", order.getOrderDate());
            map.put("totalAmount", order.getTotalAmount());
            map.put("paymentMethod", order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null);
            map.put("customerName", order.getCustomer() != null ? order.getCustomer().getName() : null);
            map.put("status", order.getStatus() != null ? order.getStatus().name() : null);
            map.put("notes", order.getNotes());

            List<OrderLineItem> items = lineItemRepository.findBySalesOrderId(order.getId());
            List<Map<String, Object>> lineItems = new ArrayList<>();
            for (OrderLineItem item : items) {
                Map<String, Object> line = new LinkedHashMap<>();
                line.put("productName", item.getFinishedProduct() != null ? item.getFinishedProduct().getProductName() : "—");
                line.put("quantity", item.getQuantity());
                line.put("unitPrice", item.getUnitPrice());
                line.put("lineTotal", item.getLineTotal());
                lineItems.add(line);
            }
            map.put("lineItems", lineItems);
            map.put("itemCount", items.size());
            return map;
        });
    }

    public Map<String, Object> getInvoice(UUID orderId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        List<OrderLineItem> items = lineItemRepository.findBySalesOrderId(orderId);

        List<Map<String, Object>> lineItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderLineItem item : items) {
            Map<String, Object> line = new LinkedHashMap<>();
            line.put("productName", item.getFinishedProduct().getProductName());
            line.put("quantity", item.getQuantity());
            line.put("unitPrice", item.getUnitPrice());
            line.put("lineTotal", item.getLineTotal());
            lineItems.add(line);
            subtotal = subtotal.add(item.getLineTotal());
        }

        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("id", order.getCustomer() == null ? null : order.getCustomer().getId());
        customer.put("name", order.getCustomer() == null ? null : order.getCustomer().getName());
        customer.put("email", order.getCustomer() == null ? null : order.getCustomer().getEmail());
        customer.put("phone", order.getCustomer() == null ? null : order.getCustomer().getPhone());

        Map<String, Object> invoice = new LinkedHashMap<>();
        invoice.put("orderNumber", order.getOrderNumber());
        invoice.put("orderDate", order.getOrderDate());
        invoice.put("customer", customer);
        invoice.put("lineItems", lineItems);
        invoice.put("subtotal", subtotal);
        invoice.put("totalAmount", order.getTotalAmount());
        invoice.put("paymentMethod", order.getPaymentMethod());
        invoice.put("status", order.getStatus());
        return invoice;
    }

    @Transactional
    public int confirm(UUID orderId, String actorEmail) {
        var order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOperationException("Only pending orders can be confirmed");
        }

        User actor = resolveActor(order.getCreatedByUser(), actorEmail);
        List<OrderLineItem> items = lineItemRepository.findBySalesOrderId(orderId);
        if (items.isEmpty()) {
            throw new InvalidOperationException("Order has no line items");
        }

        for (OrderLineItem item : items) {
            FinishedProduct product = finishedProductRepository.findById(item.getFinishedProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            BigDecimal next = product.getQuantity().subtract(item.getQuantity());
            if (next.compareTo(BigDecimal.ZERO) < 0) {
                throw new InsufficientStockException("Insufficient stock for: " + product.getProductName());
            }

            product.setQuantity(next);
            if (next.compareTo(BigDecimal.ZERO) == 0) {
                product.setStatus(FinishedProductStatus.OUT_OF_STOCK);
            }
            finishedProductRepository.save(product);

            finishedProductMovementRepository.save(FinishedProductMovement.builder()
                    .finishedProduct(product)
                    .recordedBy(actor)
                    .type(FinishedProductMovementType.SALES_OUT)
                    .quantity(item.getQuantity())
                    .date(OffsetDateTime.now())
                    .referenceId(order.getId())
                    .notes("Auto stock deduction on order confirmation")
                    .build());
        }

        order.setStatus(OrderStatus.CONFIRMED);
        salesOrderRepository.save(order);
        notificationService.notifyOrderConfirmed(order);
        log.info("Confirmed order {} with {} items", order.getOrderNumber(), items.size());
        return items.size();
    }

    @Transactional
    public int cancel(UUID orderId, String actorEmail) {
        var order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        User actor = resolveActor(order.getCreatedByUser(), actorEmail);

        List<OrderLineItem> items = lineItemRepository.findBySalesOrderId(orderId);

        if (order.getStatus() == OrderStatus.CONFIRMED) {
            for (OrderLineItem item : items) {
                FinishedProduct product = finishedProductRepository.findById(item.getFinishedProduct().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
                product.setQuantity(product.getQuantity().add(item.getQuantity()));
                if (product.getStatus() == FinishedProductStatus.OUT_OF_STOCK && product.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                    product.setStatus(FinishedProductStatus.AVAILABLE);
                } else if (product.getStatus() == FinishedProductStatus.EXPIRED && product.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                    product.setStatus(FinishedProductStatus.AVAILABLE);
                }
                finishedProductRepository.save(product);

                finishedProductMovementRepository.save(FinishedProductMovement.builder()
                        .finishedProduct(product)
                        .recordedBy(actor)
                        .type(FinishedProductMovementType.ADJUSTMENT)
                        .quantity(item.getQuantity())
                        .date(OffsetDateTime.now())
                        .referenceId(order.getId())
                        .notes("Stock restoration after confirmed order cancellation")
                        .build());
            }
        } else if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOperationException("Only pending or confirmed orders can be cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        salesOrderRepository.save(order);
        log.info("Cancelled order {}", order.getOrderNumber());
        return items.size();
    }

    @Transactional
    public void ship(UUID orderId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidOperationException("Only confirmed orders can be shipped");
        }
        order.setStatus(OrderStatus.SHIPPED);
        salesOrderRepository.save(order);
        log.info("Shipped order {}", order.getOrderNumber());
    }

    @Transactional
    public void deliver(UUID orderId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new InvalidOperationException("Only shipped orders can be delivered");
        }
        order.setStatus(OrderStatus.DELIVERED);
        salesOrderRepository.save(order);
        log.info("Delivered order {}", order.getOrderNumber());
    }

    private User resolveActor(User creator, String fallbackEmail) {
        if (creator != null) {
            return creator;
        }
        return userRepository.findByEmail(fallbackEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Actor user not found"));
    }
}
