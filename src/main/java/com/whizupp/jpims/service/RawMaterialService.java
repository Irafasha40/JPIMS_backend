package com.whizupp.jpims.service;

import com.whizupp.jpims.entity.RawMaterial;
import com.whizupp.jpims.entity.StockMovement;
import com.whizupp.jpims.entity.User;
import com.whizupp.jpims.enums.DomainEnums.StockMovementType;
import com.whizupp.jpims.exception.InsufficientStockException;
import com.whizupp.jpims.exception.InvalidOperationException;
import com.whizupp.jpims.exception.ResourceNotFoundException;
import com.whizupp.jpims.repository.RawMaterialRepository;
import com.whizupp.jpims.repository.StockMovementRepository;
import com.whizupp.jpims.repository.UserRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RawMaterialService {
    private final RawMaterialRepository rawMaterialRepository;
    private final StockMovementRepository stockMovementRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<RawMaterial> list(Pageable pageable) {
        return rawMaterialRepository.findAll(pageable);
    }

    public RawMaterial get(UUID id) {
        return rawMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Raw material not found"));
    }

    @Transactional
    public RawMaterial create(RawMaterial rawMaterial) {
        rawMaterial.setId(null);
        if (rawMaterial.getCurrentStock() == null || rawMaterial.getCurrentStock().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidOperationException("Current stock must be a non-negative value");
        }
        if (rawMaterial.getMinimumThreshold() == null) {
            rawMaterial.setMinimumThreshold(BigDecimal.ZERO);
        }
        if (rawMaterial.getIsActive() == null) {
            rawMaterial.setIsActive(true);
        }
        return rawMaterialRepository.save(rawMaterial);
    }

    @Transactional
    public RawMaterial update(UUID id, RawMaterial request) {
        RawMaterial material = get(id);
        material.setName(request.getName());
        material.setCategory(request.getCategory());
        material.setUnitOfMeasure(request.getUnitOfMeasure());
        material.setMinimumThreshold(request.getMinimumThreshold());
        material.setCostPerUnit(request.getCostPerUnit());
        material.setSupplier(request.getSupplier());
        return rawMaterialRepository.save(material);
    }

    @Transactional
    public void softDelete(UUID id) {
        RawMaterial material = get(id);
        material.setIsActive(false);
        rawMaterialRepository.save(material);
    }

    @Transactional
    public RawMaterial stockIn(UUID id, String quantityRaw, String notes, String actorEmail) {
        RawMaterial material = get(id);
        User actor = resolveUser(actorEmail);
        BigDecimal quantity = parsePositiveQuantity(quantityRaw);

        material.setCurrentStock(material.getCurrentStock().add(quantity));
        RawMaterial saved = rawMaterialRepository.save(material);

        stockMovementRepository.save(StockMovement.builder()
                .rawMaterial(saved)
                .recordedBy(actor)
                .type(StockMovementType.STOCK_IN)
                .quantity(quantity)
                .referenceNumber("MANUAL-STOCK-IN")
                .date(OffsetDateTime.now())
                .notes(notes)
                .build());

        return saved;
    }

    @Transactional
    public RawMaterial stockOut(UUID id, String quantityRaw, String notes, String actorEmail) {
        RawMaterial material = get(id);
        User actor = resolveUser(actorEmail);
        BigDecimal quantity = parsePositiveQuantity(quantityRaw);

        BigDecimal nextStock = material.getCurrentStock().subtract(quantity);
        if (nextStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientStockException("Insufficient stock for: " + material.getName()
                    + ". Required: " + quantity + ", Available: " + material.getCurrentStock());
        }

        material.setCurrentStock(nextStock);
        RawMaterial saved = rawMaterialRepository.save(material);

        stockMovementRepository.save(StockMovement.builder()
                .rawMaterial(saved)
                .recordedBy(actor)
                .type(StockMovementType.STOCK_OUT)
                .quantity(quantity)
                .referenceNumber("MANUAL-STOCK-OUT")
                .date(OffsetDateTime.now())
                .notes(notes)
                .build());

        if (saved.getCurrentStock().compareTo(saved.getMinimumThreshold()) <= 0) {
            notificationService.notifyLowStock(saved);
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public Page<StockMovement> movements(UUID rawMaterialId, Pageable pageable) {
        return stockMovementRepository.findByRawMaterial_Id(rawMaterialId, pageable);
    }

    public Page<RawMaterial> lowStock(Pageable pageable) {
        List<RawMaterial> lowStock = rawMaterialRepository.findLowStockMaterials();
        int start = Math.min((int) pageable.getOffset(), lowStock.size());
        int end = Math.min(start + pageable.getPageSize(), lowStock.size());
        return new PageImpl<>(lowStock.subList(start, end), pageable, lowStock.size());
    }

    private User resolveUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private BigDecimal parsePositiveQuantity(String quantityRaw) {
        try {
            BigDecimal quantity = new BigDecimal(quantityRaw);
            if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidOperationException("Quantity must be greater than zero");
            }
            return quantity;
        } catch (NumberFormatException ex) {
            throw new InvalidOperationException("Invalid quantity value");
        }
    }
}
