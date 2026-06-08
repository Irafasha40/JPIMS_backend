package com.whizupp.jpims.service;

import com.whizupp.jpims.dto.request.PurchaseOrderCreateRequest;
import com.whizupp.jpims.dto.request.PurchaseOrderReceiveRequest;
import com.whizupp.jpims.dto.response.PurchaseOrderDetailResponse;
import com.whizupp.jpims.dto.response.PurchaseOrderSummaryResponse;
import com.whizupp.jpims.entity.PurchaseOrder;
import com.whizupp.jpims.entity.PurchaseOrderItem;
import com.whizupp.jpims.entity.RawMaterial;
import com.whizupp.jpims.entity.StockMovement;
import com.whizupp.jpims.entity.Supplier;
import com.whizupp.jpims.entity.User;
import com.whizupp.jpims.enums.DomainEnums.PurchaseOrderStatus;
import com.whizupp.jpims.enums.DomainEnums.StockMovementType;
import com.whizupp.jpims.exception.InvalidOperationException;
import com.whizupp.jpims.exception.ResourceNotFoundException;
import com.whizupp.jpims.repository.PurchaseOrderItemRepository;
import com.whizupp.jpims.repository.PurchaseOrderRepository;
import com.whizupp.jpims.repository.RawMaterialRepository;
import com.whizupp.jpims.repository.StockMovementRepository;
import com.whizupp.jpims.repository.SupplierRepository;
import com.whizupp.jpims.repository.UserRepository;
import com.whizupp.jpims.util.PurchaseOrderNumberGenerator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseOrderService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final SupplierRepository supplierRepository;
    private final RawMaterialRepository rawMaterialRepository;
    private final UserRepository userRepository;
    private final StockMovementRepository stockMovementRepository;
    private final PurchaseOrderNumberGenerator purchaseOrderNumberGenerator;

    @Transactional(readOnly = true)
    public Page<PurchaseOrderSummaryResponse> list(Pageable pageable) {
        Page<PurchaseOrder> source = purchaseOrderRepository.findAll(pageable);
        List<PurchaseOrderSummaryResponse> mapped = new ArrayList<>(source.getContent().size());
        for (PurchaseOrder p : source.getContent()) {
            mapped.add(toSummary(p));
        }
        return new PageImpl<>(mapped, source.getPageable(), source.getTotalElements());
    }

    private PurchaseOrderSummaryResponse toSummary(PurchaseOrder p) {
        Hibernate.initialize(p.getSupplier());
        String supplierName = p.getSupplier() != null ? p.getSupplier().getName() : "—";
        return PurchaseOrderSummaryResponse.builder()
                .id(p.getId())
                .poNumber(p.getPoNumber())
                .supplierName(supplierName)
                .status(p.getStatus())
                .expectedDeliveryDate(p.getExpectedDeliveryDate())
                .actualDeliveryDate(p.getActualDeliveryDate())
                .totalCost(p.getTotalCost())
                .createdAt(p.getCreatedAt())
                .build();
    }

    public PurchaseOrder get(UUID id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found"));
    }

    @Transactional(readOnly = true)
    public PurchaseOrderDetailResponse getPurchaseOrderDetail(UUID id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found"));
        Hibernate.initialize(po.getSupplier());
        String supplierName = po.getSupplier() != null ? po.getSupplier().getName() : "—";
        List<PurchaseOrderItem> lines = purchaseOrderItemRepository.findByPurchaseOrderIdWithMaterial(id);
        List<PurchaseOrderDetailResponse.ItemLine> itemLines = lines.stream()
                .map(i -> PurchaseOrderDetailResponse.ItemLine.builder()
                        .id(i.getId())
                        .materialId(i.getRawMaterial().getId())
                        .materialName(i.getRawMaterial().getName())
                        .unitOfMeasure(i.getRawMaterial().getUnitOfMeasure())
                        .quantity(i.getQuantity())
                        .unitCost(i.getUnitCost())
                        .receivedQuantity(i.getReceivedQuantity() != null ? i.getReceivedQuantity() : BigDecimal.ZERO)
                        .build())
                .toList();
        return PurchaseOrderDetailResponse.builder()
                .id(po.getId())
                .poNumber(po.getPoNumber())
                .supplierName(supplierName)
                .status(po.getStatus())
                .expectedDeliveryDate(po.getExpectedDeliveryDate())
                .actualDeliveryDate(po.getActualDeliveryDate())
                .notes(po.getNotes())
                .totalCost(po.getTotalCost())
                .items(itemLines)
                .build();
    }

    @Transactional
    public PurchaseOrder create(PurchaseOrderCreateRequest body, String actorEmail) {
        if (body.getItems() == null || body.getItems().isEmpty()) {
            throw new InvalidOperationException("supplierId and items are required");
        }

        Supplier supplier = supplierRepository.findById(body.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));

        PurchaseOrder purchaseOrder = purchaseOrderRepository.save(PurchaseOrder.builder()
                .supplier(supplier)
                .poNumber(purchaseOrderNumberGenerator.generate())
                .status(PurchaseOrderStatus.PENDING)
                .expectedDeliveryDate(body.getExpectedDeliveryDate())
                .notes(body.getNotes())
                .totalCost(BigDecimal.ZERO)
                .build());

        BigDecimal totalCost = BigDecimal.ZERO;
        for (PurchaseOrderCreateRequest.Item item : body.getItems()) {
            RawMaterial material = rawMaterialRepository.findById(item.getMaterialId())
                    .orElseThrow(() -> new ResourceNotFoundException("Raw material not found"));

            purchaseOrderItemRepository.save(PurchaseOrderItem.builder()
                    .purchaseOrder(purchaseOrder)
                    .rawMaterial(material)
                    .quantity(item.getQuantity())
                    .unitCost(item.getUnitCost())
                    .receivedQuantity(BigDecimal.ZERO)
                    .build());

            totalCost = totalCost.add(item.getQuantity().multiply(item.getUnitCost()));
        }

        purchaseOrder.setTotalCost(totalCost);
        return purchaseOrderRepository.save(purchaseOrder);
    }

    @Transactional
    public PurchaseOrder receive(UUID purchaseOrderId, PurchaseOrderReceiveRequest body, String actorEmail) {
        PurchaseOrder purchaseOrder = get(purchaseOrderId);
        if (purchaseOrder.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new InvalidOperationException("Cancelled purchase order cannot be received");
        }
        if (purchaseOrder.getStatus() == PurchaseOrderStatus.RECEIVED) {
            throw new InvalidOperationException("Purchase order is already fully received");
        }

        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Actor user not found"));

        List<PurchaseOrderItem> items = purchaseOrderItemRepository.findByPurchaseOrder_Id(purchaseOrderId);
        if (items.isEmpty()) {
            throw new InvalidOperationException("Purchase order has no items");
        }

        String movementNoteBase = "Stock-in from purchase order receipt";
        String movementNotes = movementNoteBase;
        if (body != null && body.getNotes() != null && !body.getNotes().isBlank()) {
            movementNotes = movementNoteBase + ". " + body.getNotes().trim();
        }

        Map<UUID, BigDecimal> receiveByItemId = null;
        if (body != null && body.getItems() != null && !body.getItems().isEmpty()) {
            receiveByItemId = body.getItems().stream()
                    .collect(Collectors.toMap(PurchaseOrderReceiveRequest.ItemReceive::getItemId,
                            PurchaseOrderReceiveRequest.ItemReceive::getReceivedQuantity,
                            (a, b) -> b));
        }

        for (PurchaseOrderItem item : items) {
            BigDecimal remaining = item.getQuantity().subtract(item.getReceivedQuantity() == null ? BigDecimal.ZERO : item.getReceivedQuantity());
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal toReceive = receiveByItemId == null ? remaining : receiveByItemId.getOrDefault(item.getId(), BigDecimal.ZERO);
            if (toReceive.compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidOperationException("Received quantity cannot be negative");
            }
            if (toReceive.compareTo(remaining) > 0) {
                throw new InvalidOperationException("Received quantity exceeds pending amount for item " + item.getId());
            }
            if (toReceive.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            BigDecimal newReceived = (item.getReceivedQuantity() == null ? BigDecimal.ZERO : item.getReceivedQuantity()).add(toReceive);
            item.setReceivedQuantity(newReceived);
            purchaseOrderItemRepository.save(item);

            RawMaterial material = item.getRawMaterial();
            material.setCurrentStock(material.getCurrentStock().add(toReceive));
            rawMaterialRepository.save(material);

            stockMovementRepository.save(StockMovement.builder()
                    .rawMaterial(material)
                    .recordedBy(actor)
                    .purchaseOrder(purchaseOrder)
                    .type(StockMovementType.STOCK_IN)
                    .quantity(toReceive)
                    .referenceNumber(purchaseOrder.getPoNumber())
                    .date(OffsetDateTime.now())
                    .notes(movementNotes)
                    .build());
        }

        boolean fullyReceived = purchaseOrderItemRepository.findByPurchaseOrder_Id(purchaseOrderId).stream()
                .allMatch(i -> (i.getReceivedQuantity() == null ? BigDecimal.ZERO : i.getReceivedQuantity()).compareTo(i.getQuantity()) >= 0);

        purchaseOrder.setStatus(fullyReceived ? PurchaseOrderStatus.RECEIVED : PurchaseOrderStatus.PARTIAL);
        purchaseOrder.setActualDeliveryDate(LocalDate.now());
        log.info("Purchase order {} receipt processed, status {}", purchaseOrder.getPoNumber(), purchaseOrder.getStatus());
        return purchaseOrderRepository.save(purchaseOrder);
    }

    @Transactional
    public PurchaseOrder cancel(UUID id) {
        PurchaseOrder purchaseOrder = get(id);
        if (purchaseOrder.getStatus() == PurchaseOrderStatus.RECEIVED) {
            throw new InvalidOperationException("Received purchase order cannot be cancelled");
        }
        if (purchaseOrder.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new InvalidOperationException("Purchase order is already cancelled");
        }
        purchaseOrder.setStatus(PurchaseOrderStatus.CANCELLED);
        return purchaseOrderRepository.save(purchaseOrder);
    }
}
