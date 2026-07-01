package com.whizupp.jpims.service;

import com.whizupp.jpims.dto.response.FinishedProductResponse;
import com.whizupp.jpims.entity.FinishedProduct;
import com.whizupp.jpims.entity.FinishedProductMovement;
import com.whizupp.jpims.entity.ProductionBatch;
import com.whizupp.jpims.entity.RawMaterial;
import com.whizupp.jpims.entity.User;
import com.whizupp.jpims.enums.DomainEnums.BatchStatus;
import com.whizupp.jpims.enums.DomainEnums.FinishedProductMovementType;
import com.whizupp.jpims.enums.DomainEnums.FinishedProductStatus;
import com.whizupp.jpims.enums.DomainEnums.TestResult;
import com.whizupp.jpims.exception.InvalidOperationException;
import com.whizupp.jpims.exception.ResourceNotFoundException;
import com.whizupp.jpims.repository.FinishedProductMovementRepository;
import com.whizupp.jpims.repository.FinishedProductRepository;
import com.whizupp.jpims.repository.ProductionBatchRepository;
import com.whizupp.jpims.repository.QualityTestRepository;
import com.whizupp.jpims.repository.RawMaterialRepository;
import com.whizupp.jpims.repository.UserRepository;
import com.whizupp.jpims.service.PackagingService.PackagingPlan;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinishedProductService {
    private final ProductionBatchRepository batchRepository;
    private final QualityTestRepository qualityTestRepository;
    private final FinishedProductRepository finishedProductRepository;
    private final FinishedProductMovementRepository finishedProductMovementRepository;
    private final RawMaterialRepository rawMaterialRepository;
    private final RawMaterialService rawMaterialService;
    private final UserRepository userRepository;
    private final PackagingService packagingService;
    private final ProductCatalogService productCatalogService;

    @Value("${app.packaging.bottle-material-name:Empty Bottle (1L)}")
    private String bottleMaterialName;

    @Value("${app.packaging.box-material-name:Carton Box (12 bottles)}")
    private String boxMaterialName;

    @Value("${app.inventory.finished-product-shelf-days:180}")
    private int shelfLifeDays;

    @Transactional(readOnly = true)
    public Page<FinishedProductResponse> list(Pageable pageable) {
        return finishedProductRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public FinishedProductResponse getById(UUID id) {
        return finishedProductRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Finished product not found"));
    }

    /**
     * Called automatically when a batch passes QC and is marked COMPLETED.
     * Issues packaging stock and creates finished-goods inventory (in bottles).
     */
    @Transactional
    public FinishedProduct receiveFromCompletedBatch(ProductionBatch batch, String actorEmail) {
        if (batch.getStatus() != BatchStatus.COMPLETED) {
            throw new InvalidOperationException("Batch must be completed before transfer to finished goods");
        }
        if (!qualityTestRepository.existsByProductionBatchIdAndResult(batch.getId(), TestResult.PASS)) {
            throw new InvalidOperationException("Batch must have a passing QC test");
        }
        if (finishedProductRepository.existsByProductionBatch_Id(batch.getId())) {
            return finishedProductRepository.findByProductionBatch_Id(batch.getId()).orElseThrow();
        }
        if (batch.getActualYield() == null || batch.getActualYield().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Batch actual yield is required for finished goods transfer");
        }

        PackagingPlan plan = packagingService.plan(batch.getActualYield());
        issuePackaging(plan, batch, actorEmail);

        String lotNumber = batch.getBatchNumber();
        LocalDate productionDate = batch.getProductionDate() != null ? batch.getProductionDate() : LocalDate.now();
        Integer shelfLifeDays = batch.getRecipe() != null && batch.getRecipe().getShelfLifeDays() != null
                ? batch.getRecipe().getShelfLifeDays()
                : this.shelfLifeDays;
        LocalDate expiry = productionDate.plusDays(shelfLifeDays);

        // Auto-apply unit cost from product catalog if a matching entry exists
        BigDecimal catalogCost = productCatalogService.findCostByProductName(batch.getProductName())
                .orElse(null);
        if (catalogCost != null) {
            log.info("Auto-applying catalog unit cost {} for product '{}'",
                    catalogCost, batch.getProductName());
        }

        FinishedProduct product = FinishedProduct.builder()
                .productionBatch(batch)
                .productName(batch.getProductName())
                .packagingSize(plan.getUnitLabel() + " — " + plan.getRuleSummary())
                .lotNumber(lotNumber)
                .quantity(BigDecimal.valueOf(plan.getBottlesRequired()))
                .volumeLiters(plan.getVolumeLiters())
                .bottlesUsed(plan.getBottlesRequired())
                .boxesUsed(plan.getBoxesRequired())
                .expiryDate(expiry)
                .storageLocation("Cold Store A")
                .status(FinishedProductStatus.AVAILABLE)
                .unitCost(catalogCost)
                .build();

        FinishedProduct saved = finishedProductRepository.save(product);

        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        finishedProductMovementRepository.save(FinishedProductMovement.builder()
                .finishedProduct(saved)
                .recordedBy(actor)
                .type(FinishedProductMovementType.PRODUCTION_IN)
                .quantity(saved.getQuantity())
                .date(OffsetDateTime.now())
                .notes("Auto-received from batch " + batch.getBatchNumber()
                        + " — " + plan.getBottlesRequired() + " bottles, "
                        + plan.getBoxesRequired() + " boxes")
                .referenceId(batch.getId())
                .build());

        log.info(
                "Finished goods created for batch {} — {} bottles ({} L), {} boxes",
                batch.getBatchNumber(),
                plan.getBottlesRequired(),
                plan.getVolumeLiters(),
                plan.getBoxesRequired());

        return saved;
    }

    @Transactional
    public FinishedProduct transfer(UUID batchId, String lotNumber, String actorEmail) {
        ProductionBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));
        if (lotNumber != null && !lotNumber.isBlank()) {
            // manual override only when not already transferred
        }
        return receiveFromCompletedBatch(batch, actorEmail);
    }

    private void issuePackaging(PackagingPlan plan, ProductionBatch batch, String actorEmail) {
        RawMaterial bottles = rawMaterialRepository.findFirstByNameIgnoreCase(bottleMaterialName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Packaging material not found: " + bottleMaterialName
                                + ". Add it under Raw Materials (category PACKAGING)."));
        RawMaterial boxes = rawMaterialRepository.findFirstByNameIgnoreCase(boxMaterialName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Packaging material not found: " + boxMaterialName));

        String batchRef = batch.getBatchNumber();
        rawMaterialService.stockOut(
                bottles.getId(),
                String.valueOf(plan.getBottlesRequired()),
                "Packaging for finished batch " + batchRef + " (" + plan.getBottlesRequired() + " bottles)",
                actorEmail);
        rawMaterialService.stockOut(
                boxes.getId(),
                String.valueOf(plan.getBoxesRequired()),
                "Packaging for finished batch " + batchRef + " (" + plan.getBoxesRequired() + " boxes)",
                actorEmail);
    }

    public FinishedProductResponse toResponse(FinishedProduct product) {
        ProductionBatch batch = product.getProductionBatch();
        return FinishedProductResponse.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .flavor(product.getFlavor())
                .packagingSize(product.getPackagingSize())
                .lotNumber(product.getLotNumber())
                .quantity(product.getQuantity())
                .volumeLiters(product.getVolumeLiters())
                .bottlesUsed(product.getBottlesUsed())
                .boxesUsed(product.getBoxesUsed())
                .expiryDate(product.getExpiryDate())
                .storageLocation(product.getStorageLocation())
                .status(product.getStatus() != null ? product.getStatus().name() : null)
                .unitCost(product.getUnitCost())
                .batchId(batch != null ? batch.getId() : null)
                .batchNumber(batch != null ? batch.getBatchNumber() : null)
                .build();
    }

    @Transactional
    public FinishedProduct updateStatus(UUID id, FinishedProductStatus newStatus, String actorEmail) {
        FinishedProduct product = finishedProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Finished product not found"));
        
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        FinishedProductStatus oldStatus = product.getStatus();
        product.setStatus(newStatus);
        FinishedProduct saved = finishedProductRepository.save(product);
        
        finishedProductMovementRepository.save(FinishedProductMovement.builder()
                .finishedProduct(saved)
                .recordedBy(actor)
                .type(FinishedProductMovementType.ADJUSTMENT)
                .quantity(BigDecimal.ZERO)
                .date(OffsetDateTime.now())
                .notes("Status changed from " + oldStatus + " to " + newStatus)
                .build());
        
        log.info("Finished product {} status changed from {} to {}", saved.getId(), oldStatus, newStatus);
        return saved;
    }
}
