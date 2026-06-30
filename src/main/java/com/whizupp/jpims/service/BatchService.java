package com.whizupp.jpims.service;

import com.whizupp.jpims.dto.response.ProductionBatchResponse;
import com.whizupp.jpims.entity.BatchIngredient;
import com.whizupp.jpims.entity.ProductionBatch;
import com.whizupp.jpims.entity.RawMaterial;
import com.whizupp.jpims.entity.Recipe;
import com.whizupp.jpims.entity.RecipeIngredient;
import com.whizupp.jpims.entity.StockMovement;
import com.whizupp.jpims.entity.User;
import com.whizupp.jpims.enums.DomainEnums.BatchStatus;
import com.whizupp.jpims.enums.DomainEnums.StockMovementType;
import com.whizupp.jpims.exception.InsufficientStockException;
import com.whizupp.jpims.exception.InvalidOperationException;
import com.whizupp.jpims.exception.ResourceNotFoundException;
import com.whizupp.jpims.repository.BatchIngredientRepository;
import com.whizupp.jpims.repository.ProductionBatchRepository;
import com.whizupp.jpims.repository.RawMaterialRepository;
import com.whizupp.jpims.repository.RecipeIngredientRepository;
import com.whizupp.jpims.repository.RecipeRepository;
import com.whizupp.jpims.repository.StockMovementRepository;
import com.whizupp.jpims.repository.UserRepository;
import com.whizupp.jpims.util.BatchNumberGenerator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
public class BatchService {
    private final BatchIngredientRepository batchIngredientRepository;
    private final RawMaterialRepository rawMaterialRepository;
    private final ProductionBatchRepository productionBatchRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final StockMovementRepository stockMovementRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final BatchNumberGenerator batchNumberGenerator;
    private final BatchCompletionService batchCompletionService;

    @Transactional(readOnly = true)
    public Page<ProductionBatchResponse> list(Pageable pageable, String statusFilter, String productNameFilter) {
        Page<ProductionBatch> source = productionBatchRepository.findAll(pageable);
        List<ProductionBatchResponse> mapped = source.getContent().stream()
                .filter(b -> matchesFilters(b, statusFilter, productNameFilter))
                .map(b -> {
                    List<BatchIngredient> ingredients =
                            batchIngredientRepository.findByProductionBatchIdWithMaterial(b.getId());
                    return toResponse(b, ingredients);
                })
                .toList();
        return new PageImpl<>(mapped, source.getPageable(), source.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ProductionBatchResponse getBatch(UUID id) {
        ProductionBatch batch = productionBatchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));
        List<BatchIngredient> ingredients = batchIngredientRepository.findByProductionBatchIdWithMaterial(id);
        return toResponse(batch, ingredients);
    }

    private boolean matchesFilters(ProductionBatch batch, String statusFilter, String productNameFilter) {
        if (statusFilter != null && !statusFilter.isBlank()) {
            String expected = statusFilter.trim().toUpperCase().replace("-", "_");
            if (batch.getStatus() == null || !batch.getStatus().name().equals(expected)) {
                return false;
            }
        }
        if (productNameFilter != null && !productNameFilter.isBlank()) {
            String needle = productNameFilter.trim().toLowerCase();
            String name = batch.getProductName() != null ? batch.getProductName().toLowerCase() : "";
            if (!name.contains(needle)) {
                return false;
            }
        }
        return true;
    }

    private ProductionBatchResponse toResponse(ProductionBatch batch, List<BatchIngredient> ingredients) {
        List<ProductionBatchResponse.IngredientLine> lines = ingredients.stream()
                .map(ing -> ProductionBatchResponse.IngredientLine.builder()
                        .materialId(ing.getRawMaterial().getId())
                        .materialName(ing.getRawMaterial().getName())
                        .quantityRequired(ing.getQuantityRequired())
                        .quantityIssued(ing.getQuantityIssued())
                        .unitOfMeasure(ing.getRawMaterial().getUnitOfMeasure())
                        .issued(Boolean.TRUE.equals(ing.getIsIssued()))
                        .build())
                .toList();

        return ProductionBatchResponse.builder()
                .id(batch.getId())
                .batchNumber(batch.getBatchNumber())
                .productName(batch.getProductName())
                .targetQuantity(batch.getTargetQuantity())
                .actualYield(batch.getActualYield())
                .loss(batch.getLoss())
                .lossReason(batch.getLossReason())
                .status(batch.getStatus() != null ? batch.getStatus().name() : null)
                .productionDate(batch.getProductionDate())
                .recipeId(batch.getRecipe() != null ? batch.getRecipe().getId() : null)
                .recipeName(batch.getRecipe() != null ? batch.getRecipe().getName() : null)
                .shelfLifeDays(batch.getRecipe() != null ? batch.getRecipe().getShelfLifeDays() : null)
                .assignedTo(batch.getCreatedBy() != null ? batch.getCreatedBy() : "—")
                .finishedGoodsTransferred(batchCompletionService.hasFinishedGoods(batch.getId()))
                .stockApproved(Boolean.TRUE.equals(batch.getStockApproved()))
                .ingredients(lines)
                .build();
    }

    @Transactional
    public Map<String, Object> createBatch(Map<String, Object> request) {
        String recipeIdRaw = String.valueOf(request.get("recipeId"));
        String targetQuantityRaw = String.valueOf(request.get("targetQuantity"));
        if (recipeIdRaw == null || "null".equals(recipeIdRaw) || targetQuantityRaw == null || "null".equals(targetQuantityRaw)) {
            throw new InvalidOperationException("recipeId and targetQuantity are required");
        }

        UUID recipeId = UUID.fromString(recipeIdRaw);
        BigDecimal targetQuantity = new BigDecimal(targetQuantityRaw);
        if (targetQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("targetQuantity must be greater than zero");
        }

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found"));
        if (recipe.getBaseQuantity() == null || recipe.getBaseQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Recipe baseQuantity must be greater than zero");
        }

        ProductionBatch batch = productionBatchRepository.save(ProductionBatch.builder()
                .batchNumber(batchNumberGenerator.generate())
                .productName(recipe.getProductName())
                .targetQuantity(targetQuantity)
                .status(BatchStatus.PLANNED)
                .productionDate(LocalDate.now())
                .recipe(recipe)
                .build());

        List<Map<String, Object>> shortfalls = new ArrayList<>();
        List<RecipeIngredient> recipeIngredients = recipeIngredientRepository.findByRecipeIdWithMaterial(recipeId);
        if (recipeIngredients.isEmpty()) {
            throw new InvalidOperationException(
                    "Recipe has no ingredients. Add raw materials to the recipe before creating a production batch.");
        }
        for (RecipeIngredient recipeIngredient : recipeIngredients) {
            BigDecimal required = recipeIngredient.getQuantity()
                    .divide(recipe.getBaseQuantity(), 4, RoundingMode.HALF_UP)
                    .multiply(targetQuantity)
                    .setScale(4, RoundingMode.HALF_UP);

            batchIngredientRepository.save(BatchIngredient.builder()
                    .productionBatch(batch)
                    .rawMaterial(recipeIngredient.getRawMaterial())
                    .quantityRequired(required)
                    .quantityIssued(BigDecimal.ZERO)
                    .isIssued(false)
                    .build());

            BigDecimal currentStock = recipeIngredient.getRawMaterial().getCurrentStock();
            if (currentStock.compareTo(required) < 0) {
                Map<String, Object> shortfall = new LinkedHashMap<>();
                shortfall.put("materialId", recipeIngredient.getRawMaterial().getId());
                shortfall.put("materialName", recipeIngredient.getRawMaterial().getName());
                shortfall.put("required", required);
                shortfall.put("available", currentStock);
                shortfalls.add(shortfall);
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", batch.getId());
        response.put("batchNumber", batch.getBatchNumber());
        response.put("productName", batch.getProductName());
        response.put("targetQuantity", batch.getTargetQuantity());
        response.put("status", batch.getStatus());
        response.put("recipeId", recipeId);
        response.put("shortfalls", shortfalls);
        return response;
    }

    @Transactional
    public ProductionBatchResponse confirmIngredients(UUID batchId, String actorEmail) {
        ProductionBatch batch = productionBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (batch.getStatus() != BatchStatus.PLANNED && batch.getStatus() != BatchStatus.ISSUED) {
            throw new InvalidOperationException("Ingredients can only be confirmed for planned batches");
        }

        if (!Boolean.TRUE.equals(batch.getStockApproved())) {
            throw new InvalidOperationException("Stock must be approved by an Inventory Manager before ingredients can be confirmed.");
        }

        List<BatchIngredient> ingredients = batchIngredientRepository.findByProductionBatchIdWithMaterial(batchId);
        if (ingredients.isEmpty()) {
            throw new ResourceNotFoundException("No ingredients found for batch");
        }

        boolean allAlreadyIssued = ingredients.stream().allMatch(i -> Boolean.TRUE.equals(i.getIsIssued()));
        if (!allAlreadyIssued) {
        for (BatchIngredient ingredient : ingredients) {
            if (Boolean.TRUE.equals(ingredient.getIsIssued())) {
                continue;
            }
            RawMaterial material = ingredient.getRawMaterial();

            BigDecimal required = ingredient.getQuantityRequired();
            BigDecimal nextStock = material.getCurrentStock().subtract(required);
            if (nextStock.compareTo(BigDecimal.ZERO) < 0) {
                throw new InsufficientStockException("Insufficient stock for: " + material.getName()
                        + ". Required: " + required + ", Available: " + material.getCurrentStock());
            }

            material.setCurrentStock(nextStock);
            rawMaterialRepository.save(material);

            ingredient.setQuantityIssued(required);
            ingredient.setIsIssued(true);
            batchIngredientRepository.save(ingredient);

            stockMovementRepository.save(StockMovement.builder()
                    .rawMaterial(material)
                    .recordedBy(actor)
                    .productionBatch(batch)
                    .type(StockMovementType.STOCK_OUT)
                    .quantity(required)
                    .referenceNumber(batch.getBatchNumber())
                    .date(OffsetDateTime.now())
                    .notes("Auto stock-out from batch ingredient confirmation")
                    .build());

            if (material.getCurrentStock().compareTo(material.getMinimumThreshold()) <= 0) {
                notificationService.notifyLowStock(material);
            }
        }
        }

        batch.setStatus(BatchStatus.ISSUED);
        if (batch.getStartTime() == null) {
            batch.setStartTime(OffsetDateTime.now());
        }
        productionBatchRepository.save(batch);

        log.info("Confirmed ingredients for batch {} — status {}", batch.getBatchNumber(), batch.getStatus());
        return getBatch(batchId);
    }

    @Transactional
    public ProductionBatchResponse startProduction(UUID batchId) {
        ProductionBatch batch = productionBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));

        List<BatchIngredient> ingredients = batchIngredientRepository.findByProductionBatchId(batchId);
        boolean allIssued = !ingredients.isEmpty()
                && ingredients.stream().allMatch(i -> Boolean.TRUE.equals(i.getIsIssued()));

        if (batch.getStatus() == BatchStatus.PLANNED) {
            if (!allIssued) {
                throw new InvalidOperationException("Confirm and issue ingredients before starting production");
            }
            batch.setStatus(BatchStatus.ISSUED);
            productionBatchRepository.save(batch);
        }

        if (batch.getStatus() != BatchStatus.ISSUED) {
            throw new InvalidOperationException("Start production is only allowed after ingredients are issued");
        }

        if (!allIssued) {
            throw new InvalidOperationException("Issue all ingredients before starting production");
        }

        batch.setStatus(BatchStatus.IN_PROGRESS);
        if (batch.getStartTime() == null) {
            batch.setStartTime(OffsetDateTime.now());
        }
        productionBatchRepository.save(batch);

        log.info("Batch {} started — status IN_PROGRESS", batch.getBatchNumber());
        return getBatch(batchId);
    }

    @Transactional
    public ProductionBatchResponse recordYield(UUID batchId, Map<String, Object> request) {
        ProductionBatch batch = productionBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));

        if (batch.getStatus() != BatchStatus.IN_PROGRESS) {
            throw new InvalidOperationException("Yield can only be recorded while the batch is in production");
        }

        String actualYieldRaw = String.valueOf(request.get("actualYield"));
        if (actualYieldRaw == null || "null".equals(actualYieldRaw) || actualYieldRaw.isBlank()) {
            throw new InvalidOperationException("actualYield is required");
        }

        BigDecimal actualYield = new BigDecimal(actualYieldRaw);
        if (actualYield.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("actualYield must be greater than zero");
        }

        batch.setActualYield(actualYield);

        if (request.get("loss") != null && !"null".equals(String.valueOf(request.get("loss")))) {
            BigDecimal loss = new BigDecimal(String.valueOf(request.get("loss")));
            if (loss.compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidOperationException("loss cannot be negative");
            }
            batch.setLoss(loss);
        }

        if (request.get("lossReason") != null) {
            String reason = String.valueOf(request.get("lossReason"));
            if (!"null".equals(reason)) {
                batch.setLossReason(reason.isBlank() ? null : reason);
            }
        }

        productionBatchRepository.save(batch);
        log.info("Recorded yield {} for batch {}", actualYield, batch.getBatchNumber());
        return getBatch(batchId);
    }

    @Transactional
    public ProductionBatchResponse sendToQualityControl(UUID batchId) {
        ProductionBatch batch = productionBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));

        if (batch.getStatus() != BatchStatus.IN_PROGRESS) {
            throw new InvalidOperationException("Only in-production batches can be sent to quality control");
        }

        if (batch.getActualYield() == null || batch.getActualYield().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Record actual yield before sending the batch to QC");
        }

        batch.setStatus(BatchStatus.QC_PENDING);
        batch.setCompletionTime(OffsetDateTime.now());
        productionBatchRepository.save(batch);

        notificationService.notifyQcOfficers(batch);
        log.info("Batch {} sent to QC — status QC_PENDING", batch.getBatchNumber());
        return getBatch(batchId);
    }

    @Transactional
    public ProductionBatchResponse approveStock(UUID batchId, String actorEmail) {
        ProductionBatch batch = productionBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));
        if (batch.getStatus() != BatchStatus.PLANNED) {
            throw new InvalidOperationException("Stock can only be approved for planned batches");
        }
        batch.setStockApproved(true);
        productionBatchRepository.save(batch);
        log.info("Stock approved for batch {} by {}", batch.getBatchNumber(), actorEmail);
        return getBatch(batchId);
    }
}
