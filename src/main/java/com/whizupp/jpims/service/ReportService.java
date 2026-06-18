package com.whizupp.jpims.service;

import com.whizupp.jpims.entity.FinishedProduct;
import com.whizupp.jpims.entity.ProductionBatch;
import com.whizupp.jpims.entity.QualityTest;
import com.whizupp.jpims.entity.RawMaterial;
import com.whizupp.jpims.entity.SalesOrder;
import com.whizupp.jpims.entity.ScheduledReport;
import com.whizupp.jpims.entity.User;
import com.whizupp.jpims.entity.BatchIngredient;
import com.whizupp.jpims.entity.OrderLineItem;
import com.whizupp.jpims.entity.FinishedProductMovement;
import com.whizupp.jpims.entity.StockMovement;
import com.whizupp.jpims.entity.Recipe;
import com.whizupp.jpims.enums.DomainEnums.BatchStatus;
import com.whizupp.jpims.enums.DomainEnums.OrderStatus;
import com.whizupp.jpims.enums.DomainEnums.PaymentMethod;
import com.whizupp.jpims.enums.DomainEnums.ReportFrequency;
import com.whizupp.jpims.enums.DomainEnums.TestResult;
import com.whizupp.jpims.enums.DomainEnums.FinishedProductStatus;
import com.whizupp.jpims.enums.DomainEnums.FinishedProductMovementType;
import com.whizupp.jpims.enums.DomainEnums.StockMovementType;
import com.whizupp.jpims.repository.FinishedProductRepository;
import com.whizupp.jpims.repository.ProductionBatchRepository;
import com.whizupp.jpims.repository.QualityTestRepository;
import com.whizupp.jpims.repository.RawMaterialRepository;
import com.whizupp.jpims.repository.SalesOrderRepository;
import com.whizupp.jpims.repository.ScheduledReportRepository;
import com.whizupp.jpims.repository.UserRepository;
import com.whizupp.jpims.repository.BatchIngredientRepository;
import com.whizupp.jpims.repository.OrderLineItemRepository;
import com.whizupp.jpims.repository.StockMovementRepository;
import com.whizupp.jpims.repository.FinishedProductMovementRepository;
import com.whizupp.jpims.repository.RecipeRepository;
import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ProductionBatchRepository batchRepository;
    private final QualityTestRepository qualityTestRepository;
    private final RawMaterialRepository rawMaterialRepository;
    private final FinishedProductRepository finishedProductRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final ScheduledReportRepository scheduledReportRepository;
    private final UserRepository userRepository;
    private final BatchIngredientRepository batchIngredientRepository;
    private final OrderLineItemRepository orderLineItemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final FinishedProductMovementRepository finishedProductMovementRepository;
    private final RecipeRepository recipeRepository;

    public Map<String, Object> getProductionReport(String from, String to, UUID recipeId) {
        List<ProductionBatch> batches = batchRepository.findAll();

        if (recipeId != null) {
            Recipe recipe = recipeRepository.findById(recipeId).orElse(null);
            if (recipe != null) {
                String pName = recipe.getProductName();
                batches = batches.stream()
                        .filter(b -> b.getProductName() != null && b.getProductName().equalsIgnoreCase(pName))
                        .collect(Collectors.toList());
            }
        }

        if (from != null && !from.isEmpty()) {
            try {
                LocalDate fromDate = LocalDate.parse(from);
                batches = batches.stream()
                        .filter(b -> b.getProductionDate() != null && !b.getProductionDate().isBefore(fromDate))
                        .collect(Collectors.toList());
            } catch (Exception e) {}
        }

        if (to != null && !to.isEmpty()) {
            try {
                LocalDate toDate = LocalDate.parse(to);
                batches = batches.stream()
                        .filter(b -> b.getProductionDate() != null && !b.getProductionDate().isAfter(toDate))
                        .collect(Collectors.toList());
            } catch (Exception e) {}
        }

        long totalBatches = batches.size();
        long inProgress = 0;
        long pendingQc = 0;
        long passed = 0;
        long failed = 0;
        long transferred = 0;

        BigDecimal totalTargetVolume = BigDecimal.ZERO;
        BigDecimal totalActualYield = BigDecimal.ZERO;

        Map<String, BigDecimal> ingredientConsumption = new LinkedHashMap<>();
        Map<String, Long> batchesByJuiceVariety = new LinkedHashMap<>();

        for (ProductionBatch batch : batches) {
            BatchStatus status = batch.getStatus();
            if (status == BatchStatus.IN_PROGRESS || status == BatchStatus.ISSUED) {
                inProgress++;
            } else if (status == BatchStatus.QC_PENDING) {
                pendingQc++;
            } else if (status == BatchStatus.COMPLETED) {
                passed++;
            }

            if (qualityTestRepository.existsByProductionBatchIdAndResult(batch.getId(), TestResult.FAIL)) {
                failed++;
            }

            if (finishedProductRepository.existsByProductionBatch_Id(batch.getId())) {
                transferred++;
            }

            if (batch.getTargetQuantity() != null) {
                totalTargetVolume = totalTargetVolume.add(batch.getTargetQuantity());
            }
            if (batch.getActualYield() != null) {
                totalActualYield = totalActualYield.add(batch.getActualYield());
            }

            List<BatchIngredient> ingredients = batchIngredientRepository.findByProductionBatchId(batch.getId());
            for (BatchIngredient ing : ingredients) {
                if (Boolean.TRUE.equals(ing.getIsIssued()) && ing.getQuantityIssued() != null) {
                    String name = ing.getRawMaterial().getName();
                    ingredientConsumption.put(name, ingredientConsumption.getOrDefault(name, BigDecimal.ZERO).add(ing.getQuantityIssued()));
                }
            }

            String variety = batch.getProductName() != null ? batch.getProductName() : "Unknown Variety";
            batchesByJuiceVariety.put(variety, batchesByJuiceVariety.getOrDefault(variety, 0L) + 1);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalBatchesInitiated", totalBatches);
        summary.put("inProgress", inProgress);
        summary.put("pendingQc", pendingQc);
        summary.put("passed", passed);
        summary.put("failed", failed);
        summary.put("transferred", transferred);
        summary.put("totalTargetVolume", totalTargetVolume);
        summary.put("totalActualYield", totalActualYield);
        summary.put("ingredientConsumption", ingredientConsumption);
        summary.put("batchesByJuiceVariety", batchesByJuiceVariety);

        List<Map<String, Object>> dataList = batches.stream()
                .map(b -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", b.getId());
                    map.put("batchNumber", b.getBatchNumber());
                    map.put("productName", b.getProductName());
                    map.put("targetQuantity", b.getTargetQuantity());
                    map.put("actualYield", b.getActualYield());
                    map.put("loss", b.getLoss());
                    map.put("lossReason", b.getLossReason());
                    map.put("status", b.getStatus() != null ? b.getStatus().name() : null);
                    map.put("productionDate", b.getProductionDate() != null ? b.getProductionDate().toString() : null);
                    map.put("startTime", b.getStartTime() != null ? b.getStartTime().toString() : null);
                    map.put("completionTime", b.getCompletionTime() != null ? b.getCompletionTime().toString() : null);
                    return map;
                }).collect(Collectors.toList());

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("summary", summary);
        report.put("data", dataList);
        return report;
    }

    public Map<String, Object> getQualityReport(String from, String to, UUID productId) {
        List<QualityTest> tests = qualityTestRepository.findAll();

        if (productId != null) {
            FinishedProduct product = finishedProductRepository.findById(productId).orElse(null);
            if (product != null) {
                String pName = product.getProductName();
                tests = tests.stream()
                        .filter(t -> t.getProductionBatch() != null && t.getProductionBatch().getProductName() != null 
                                && t.getProductionBatch().getProductName().equalsIgnoreCase(pName))
                        .collect(Collectors.toList());
            } else {
                Recipe recipe = recipeRepository.findById(productId).orElse(null);
                if (recipe != null) {
                    String pName = recipe.getProductName();
                    tests = tests.stream()
                            .filter(t -> t.getProductionBatch() != null && t.getProductionBatch().getProductName() != null 
                                    && t.getProductionBatch().getProductName().equalsIgnoreCase(pName))
                            .collect(Collectors.toList());
                }
            }
        }

        if (from != null && !from.isEmpty()) {
            try {
                LocalDate fromDate = LocalDate.parse(from);
                tests = tests.stream()
                        .filter(t -> t.getTestDate() != null && !t.getTestDate().toLocalDate().isBefore(fromDate))
                        .collect(Collectors.toList());
            } catch (Exception e) {}
        }

        if (to != null && !to.isEmpty()) {
            try {
                LocalDate toDate = LocalDate.parse(to);
                tests = tests.stream()
                        .filter(t -> t.getTestDate() != null && !t.getTestDate().toLocalDate().isAfter(toDate))
                        .collect(Collectors.toList());
            } catch (Exception e) {}
        }

        long totalChecks = tests.size();
        long passed = tests.stream().filter(t -> t.getResult() == TestResult.PASS).count();
        long failed = tests.stream().filter(t -> t.getResult() == TestResult.FAIL).count();
        double passRate = totalChecks > 0 ? (double) passed / totalChecks * 100 : 0.0;
        double failRate = totalChecks > 0 ? (double) failed / totalChecks * 100 : 0.0;

        Map<String, Double> sumPh = new LinkedHashMap<>();
        Map<String, Integer> phCounts = new LinkedHashMap<>();
        Map<String, Double> sumBrix = new LinkedHashMap<>();
        Map<String, Integer> brixCounts = new LinkedHashMap<>();

        Map<String, Long> failureReasons = new LinkedHashMap<>();
        long totalReviewDurationMinutes = 0;
        long durationCounts = 0;

        for (QualityTest t : tests) {
            String pName = (t.getProductionBatch() != null && t.getProductionBatch().getProductName() != null) 
                    ? t.getProductionBatch().getProductName() : "Unknown Product";

            if (t.getPhLevel() != null) {
                sumPh.put(pName, sumPh.getOrDefault(pName, 0.0) + t.getPhLevel().doubleValue());
                phCounts.put(pName, phCounts.getOrDefault(pName, 0) + 1);
            }
            if (t.getBrixLevel() != null) {
                sumBrix.put(pName, sumBrix.getOrDefault(pName, 0.0) + t.getBrixLevel().doubleValue());
                brixCounts.put(pName, brixCounts.getOrDefault(pName, 0) + 1);
            }

            if (t.getResult() == TestResult.FAIL && t.getNotes() != null && !t.getNotes().isEmpty()) {
                String reason = t.getNotes();
                failureReasons.put(reason, failureReasons.getOrDefault(reason, 0L) + 1);
            }

            if (t.getProductionBatch() != null && t.getProductionBatch().getCompletionTime() != null && t.getTestDate() != null) {
                long duration = java.time.Duration.between(t.getProductionBatch().getCompletionTime(), t.getTestDate()).toMinutes();
                totalReviewDurationMinutes += duration;
                durationCounts++;
            }
        }

        Map<String, Double> avgPhPerProduct = new LinkedHashMap<>();
        for (String pName : sumPh.keySet()) {
            int count = phCounts.get(pName);
            avgPhPerProduct.put(pName, count > 0 ? round(sumPh.get(pName) / count, 2) : 0.0);
        }

        Map<String, Double> avgBrixPerProduct = new LinkedHashMap<>();
        for (String pName : sumBrix.keySet()) {
            int count = brixCounts.get(pName);
            avgBrixPerProduct.put(pName, count > 0 ? round(sumBrix.get(pName) / count, 2) : 0.0);
        }

        double avgTimeToQcReviewMinutes = durationCounts > 0 ? (double) totalReviewDurationMinutes / durationCounts : 0.0;

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalQcChecks", totalChecks);
        summary.put("passRate", round(passRate, 2));
        summary.put("failRate", round(failRate, 2));
        summary.put("avgPhPerProduct", avgPhPerProduct);
        summary.put("avgBrixPerProduct", avgBrixPerProduct);
        summary.put("mostCommonFailureReasons", failureReasons);
        summary.put("avgTimeToQcReviewMinutes", round(avgTimeToQcReviewMinutes, 2));

        List<Map<String, Object>> dataList = tests.stream()
                .map(t -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", t.getId());
                    map.put("batchNumber", t.getProductionBatch() != null ? t.getProductionBatch().getBatchNumber() : null);
                    map.put("productName", t.getProductionBatch() != null ? t.getProductionBatch().getProductName() : null);
                    map.put("phLevel", t.getPhLevel());
                    map.put("brixLevel", t.getBrixLevel());
                    map.put("appearance", t.getAppearance() != null ? t.getAppearance().name() : null);
                    map.put("color", t.getColor() != null ? t.getColor().name() : null);
                    map.put("taste", t.getTaste() != null ? t.getTaste().name() : null);
                    map.put("result", t.getResult() != null ? t.getResult().name() : null);
                    map.put("testDate", t.getTestDate() != null ? t.getTestDate().toString() : null);
                    map.put("testedBy", t.getTestedBy() != null ? t.getTestedBy().getFullName() : null);
                    map.put("notes", t.getNotes());

                    long reviewMinutes = 0;
                    if (t.getProductionBatch() != null && t.getProductionBatch().getCompletionTime() != null && t.getTestDate() != null) {
                        reviewMinutes = java.time.Duration.between(t.getProductionBatch().getCompletionTime(), t.getTestDate()).toMinutes();
                    }
                    map.put("timeToQcReviewMinutes", reviewMinutes);
                    return map;
                }).collect(Collectors.toList());

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("summary", summary);
        report.put("data", dataList);
        return report;
    }

    public Map<String, Object> getInventoryReport(String from, String to) {
        List<RawMaterial> rawMaterials = rawMaterialRepository.findAll();
        List<FinishedProduct> finishedProducts = finishedProductRepository.findAll();

        BigDecimal totalRawMaterialsValuation = BigDecimal.ZERO;
        long itemsBelowMinThreshold = 0;
        Map<String, BigDecimal> rawValuation = new LinkedHashMap<>();

        for (RawMaterial rm : rawMaterials) {
            BigDecimal stock = rm.getCurrentStock() != null ? rm.getCurrentStock() : BigDecimal.ZERO;
            BigDecimal cost = rm.getCostPerUnit() != null ? rm.getCostPerUnit() : BigDecimal.ZERO;
            BigDecimal val = stock.multiply(cost);
            totalRawMaterialsValuation = totalRawMaterialsValuation.add(val);
            rawValuation.put(rm.getName(), val);

            if (rm.getMinimumThreshold() != null && stock.compareTo(rm.getMinimumThreshold()) <= 0) {
                itemsBelowMinThreshold++;
            }
        }

        BigDecimal totalFinishedGoodsValuation = BigDecimal.ZERO;
        Map<String, BigDecimal> finishedValuation = new LinkedHashMap<>();
        long nearExpiry7Days = 0;
        long nearExpiry30Days = 0;

        LocalDate today = LocalDate.now();
        LocalDate plus7 = today.plusDays(7);
        LocalDate plus30 = today.plusDays(30);

        for (FinishedProduct fp : finishedProducts) {
            BigDecimal qty = fp.getQuantity() != null ? fp.getQuantity() : BigDecimal.ZERO;
            BigDecimal cost = fp.getUnitCost() != null ? fp.getUnitCost() : BigDecimal.ZERO;
            BigDecimal val = qty.multiply(cost);
            totalFinishedGoodsValuation = totalFinishedGoodsValuation.add(val);
            finishedValuation.put(fp.getProductName(), finishedValuation.getOrDefault(fp.getProductName(), BigDecimal.ZERO).add(val));

            if (fp.getExpiryDate() != null && fp.getStatus() != FinishedProductStatus.EXPIRED) {
                if (!fp.getExpiryDate().isBefore(today)) {
                    if (!fp.getExpiryDate().isAfter(plus7)) {
                        nearExpiry7Days++;
                    }
                    if (!fp.getExpiryDate().isAfter(plus30)) {
                        nearExpiry30Days++;
                    }
                }
            }
        }

        BigDecimal totalWastage = BigDecimal.ZERO;
        OffsetDateTime fromTime = null;
        OffsetDateTime toTime = null;
        try {
            if (from != null && !from.isEmpty()) fromTime = LocalDate.parse(from).atStartOfDay(java.time.ZoneId.systemDefault()).toOffsetDateTime();
            if (to != null && !to.isEmpty()) toTime = LocalDate.parse(to).atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime();
        } catch (Exception e) {}

        List<StockMovement> rmMovements = stockMovementRepository.findAll();
        for (StockMovement mv : rmMovements) {
            if (mv.getType() == StockMovementType.STOCK_OUT && mv.getNotes() != null) {
                String notes = mv.getNotes().toLowerCase();
                if (notes.contains("waste") || notes.contains("expired") || notes.contains("scrap") || notes.contains("write-off") || notes.contains("written-off")) {
                    if (fromTime != null && mv.getDate().isBefore(fromTime)) continue;
                    if (toTime != null && mv.getDate().isAfter(toTime)) continue;
                    BigDecimal cost = mv.getRawMaterial().getCostPerUnit() != null ? mv.getRawMaterial().getCostPerUnit() : BigDecimal.ZERO;
                    totalWastage = totalWastage.add(mv.getQuantity().multiply(cost));
                }
            }
        }

        List<FinishedProductMovement> fpMovements = finishedProductMovementRepository.findAll();
        for (FinishedProductMovement mv : fpMovements) {
            if ((mv.getType() == FinishedProductMovementType.ADJUSTMENT || mv.getType() == FinishedProductMovementType.RECALL) && mv.getNotes() != null) {
                String notes = mv.getNotes().toLowerCase();
                if (notes.contains("waste") || notes.contains("expired") || notes.contains("scrap") || notes.contains("write-off") || notes.contains("written-off")) {
                    if (fromTime != null && mv.getDate().isBefore(fromTime)) continue;
                    if (toTime != null && mv.getDate().isAfter(toTime)) continue;
                    BigDecimal cost = mv.getFinishedProduct().getUnitCost() != null ? mv.getFinishedProduct().getUnitCost() : BigDecimal.ZERO;
                    totalWastage = totalWastage.add(mv.getQuantity().multiply(cost));
                }
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("rawMaterialsValuation", rawValuation);
        summary.put("totalRawMaterialsValuation", totalRawMaterialsValuation);
        summary.put("itemsBelowMinThreshold", itemsBelowMinThreshold);
        summary.put("finishedGoodsValuation", finishedValuation);
        summary.put("totalFinishedGoodsValuation", totalFinishedGoodsValuation);
        summary.put("nearExpiryWithin7Days", nearExpiry7Days);
        summary.put("nearExpiryWithin30Days", nearExpiry30Days);
        summary.put("totalWastage", totalWastage);

        List<Map<String, Object>> dataList = new ArrayList<>();
        for (RawMaterial rm : rawMaterials) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("type", "RAW_MATERIAL");
            map.put("id", rm.getId());
            map.put("name", rm.getName());
            map.put("category", rm.getCategory());
            map.put("currentStock", rm.getCurrentStock());
            map.put("unit", rm.getUnitOfMeasure());
            map.put("costPerUnit", rm.getCostPerUnit());
            BigDecimal stock = rm.getCurrentStock() != null ? rm.getCurrentStock() : BigDecimal.ZERO;
            BigDecimal cost = rm.getCostPerUnit() != null ? rm.getCostPerUnit() : BigDecimal.ZERO;
            map.put("totalValue", stock.multiply(cost));
            map.put("minimumThreshold", rm.getMinimumThreshold());
            map.put("status", stock.compareTo(rm.getMinimumThreshold()) <= 0 ? "LOW_STOCK" : "OK");
            map.put("expiryDate", null);
            dataList.add(map);
        }

        for (FinishedProduct fp : finishedProducts) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("type", "FINISHED_GOOD");
            map.put("id", fp.getId());
            map.put("name", fp.getProductName());
            map.put("category", fp.getFlavor());
            map.put("currentStock", fp.getQuantity());
            map.put("unit", "Bottles");
            map.put("costPerUnit", fp.getUnitCost());
            BigDecimal qty = fp.getQuantity() != null ? fp.getQuantity() : BigDecimal.ZERO;
            BigDecimal cost = fp.getUnitCost() != null ? fp.getUnitCost() : BigDecimal.ZERO;
            map.put("totalValue", qty.multiply(cost));
            map.put("minimumThreshold", BigDecimal.ZERO);
            map.put("status", fp.getStatus() != null ? fp.getStatus().name() : "AVAILABLE");
            map.put("expiryDate", fp.getExpiryDate() != null ? fp.getExpiryDate().toString() : null);
            dataList.add(map);
        }

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("summary", summary);
        report.put("data", dataList);
        return report;
    }

    public Map<String, Object> getSalesReport(String from, String to, UUID productId, String customer) {
        List<SalesOrder> orders = salesOrderRepository.findAll();

        if (customer != null && !customer.isEmpty()) {
            orders = orders.stream()
                    .filter(o -> o.getCustomer() != null && (o.getCustomer().getId().toString().equals(customer) 
                            || o.getCustomer().getName().equalsIgnoreCase(customer)))
                    .collect(Collectors.toList());
        }

        if (from != null && !from.isEmpty()) {
            try {
                LocalDate fromDate = LocalDate.parse(from);
                orders = orders.stream()
                        .filter(o -> o.getOrderDate() != null && !o.getOrderDate().isBefore(fromDate))
                        .collect(Collectors.toList());
            } catch (Exception e) {}
        }

        if (to != null && !to.isEmpty()) {
            try {
                LocalDate toDate = LocalDate.parse(to);
                orders = orders.stream()
                        .filter(o -> o.getOrderDate() != null && !o.getOrderDate().isAfter(toDate))
                        .collect(Collectors.toList());
            } catch (Exception e) {}
        }

        long confirmedOrders = 0;
        long cancelledOrders = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;

        Map<String, BigDecimal> totalQuantitySoldByProduct = new LinkedHashMap<>();
        Map<String, Long> ordersByCustomer = new LinkedHashMap<>();
        Map<String, Long> ordersByPeriod = new LinkedHashMap<>();

        final UUID filterProductId = productId;
        List<SalesOrder> finalFilteredOrders = new ArrayList<>();

        for (SalesOrder o : orders) {
            List<OrderLineItem> items = orderLineItemRepository.findBySalesOrderId(o.getId());

            if (filterProductId != null) {
                boolean hasProduct = items.stream().anyMatch(item -> item.getFinishedProduct().getId().equals(filterProductId) 
                        || (item.getFinishedProduct().getProductName() != null 
                            && item.getFinishedProduct().getProductName().equalsIgnoreCase(filterProductId.toString())));
                if (!hasProduct) continue;
            }

            finalFilteredOrders.add(o);

            OrderStatus status = o.getStatus();
            if (status == OrderStatus.CONFIRMED || status == OrderStatus.DELIVERED || status == OrderStatus.SHIPPED) {
                confirmedOrders++;
            } else if (status == OrderStatus.CANCELLED) {
                cancelledOrders++;
            }

            if (o.getTotalAmount() != null) {
                totalRevenue = totalRevenue.add(o.getTotalAmount());
            }

            for (OrderLineItem item : items) {
                String pName = item.getFinishedProduct().getProductName();
                totalQuantitySoldByProduct.put(pName, totalQuantitySoldByProduct.getOrDefault(pName, BigDecimal.ZERO).add(item.getQuantity()));
            }

            if (o.getCustomer() != null) {
                String cName = o.getCustomer().getName();
                ordersByCustomer.put(cName, ordersByCustomer.getOrDefault(cName, 0L) + 1);
            }

            if (o.getOrderDate() != null) {
                String period = o.getOrderDate().getYear() + "-" + String.format("%02d", o.getOrderDate().getMonthValue());
                ordersByPeriod.put(period, ordersByPeriod.getOrDefault(period, 0L) + 1);
            }
        }

        double fulfillmentRate = (confirmedOrders + cancelledOrders) > 0 
                ? (double) confirmedOrders / (confirmedOrders + cancelledOrders) * 100 : 0.0;

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalOrders", finalFilteredOrders.size());
        summary.put("totalQuantitySoldByProduct", totalQuantitySoldByProduct);
        summary.put("fulfillmentRate", round(fulfillmentRate, 2));
        summary.put("confirmedOrders", confirmedOrders);
        summary.put("cancelledOrders", cancelledOrders);
        summary.put("totalRevenue", totalRevenue);
        summary.put("ordersByCustomer", ordersByCustomer);
        summary.put("ordersByPeriod", ordersByPeriod);

        List<Map<String, Object>> dataList = finalFilteredOrders.stream()
                .map(o -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", o.getId());
                    map.put("orderNumber", o.getOrderNumber());
                    map.put("customerName", o.getCustomer() != null ? o.getCustomer().getName() : null);
                    map.put("orderDate", o.getOrderDate() != null ? o.getOrderDate().toString() : null);
                    map.put("totalAmount", o.getTotalAmount());
                    map.put("status", o.getStatus() != null ? o.getStatus().name() : null);
                    map.put("paymentMethod", o.getPaymentMethod() != null ? o.getPaymentMethod().name() : null);
                    return map;
                }).collect(Collectors.toList());

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("summary", summary);
        report.put("data", dataList);
        return report;
    }

    public Map<String, Object> getWastageReport(String from, String to) {
        LocalDate toDate = (to != null && !to.isEmpty()) ? LocalDate.parse(to) : LocalDate.now();
        LocalDate fromDate = (from != null && !from.isEmpty()) ? LocalDate.parse(from) : toDate.minusDays(30);

        OffsetDateTime start = fromDate.atStartOfDay(java.time.ZoneId.systemDefault()).toOffsetDateTime();
        OffsetDateTime end = toDate.atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime();

        BigDecimal totalWastageCost = calculateWastageCostInPeriod(start, end);

        long days = java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        LocalDate compToDate = fromDate.minusDays(1);
        LocalDate compFromDate = compToDate.minusDays(days - 1);
        OffsetDateTime compStart = compFromDate.atStartOfDay(java.time.ZoneId.systemDefault()).toOffsetDateTime();
        OffsetDateTime compEnd = compToDate.atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime();

        BigDecimal comparisonPeriodWastageCost = calculateWastageCostInPeriod(compStart, compEnd);

        BigDecimal totalWastageQuantity = BigDecimal.ZERO;
        Map<String, BigDecimal> wastageByProduct = new LinkedHashMap<>();
        Map<String, BigDecimal> wastageByMaterial = new LinkedHashMap<>();

        List<Map<String, Object>> dataList = new ArrayList<>();

        List<StockMovement> rmMovements = stockMovementRepository.findAll();
        for (StockMovement mv : rmMovements) {
            if (mv.getType() == StockMovementType.STOCK_OUT && mv.getNotes() != null) {
                String notes = mv.getNotes().toLowerCase();
                if (notes.contains("waste") || notes.contains("expired") || notes.contains("scrap") || notes.contains("write-off") || notes.contains("written-off")) {
                    if (mv.getDate().isBefore(start) || mv.getDate().isAfter(end)) continue;
                    BigDecimal price = mv.getRawMaterial().getCostPerUnit() != null ? mv.getRawMaterial().getCostPerUnit() : BigDecimal.ZERO;
                    BigDecimal estCost = mv.getQuantity().multiply(price);
                    totalWastageQuantity = totalWastageQuantity.add(mv.getQuantity());

                    String name = mv.getRawMaterial().getName();
                    wastageByMaterial.put(name, wastageByMaterial.getOrDefault(name, BigDecimal.ZERO).add(mv.getQuantity()));

                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("type", "MATERIAL");
                    map.put("id", mv.getId());
                    map.put("name", name);
                    map.put("quantity", mv.getQuantity());
                    map.put("unit", mv.getRawMaterial().getUnitOfMeasure());
                    map.put("costEstimate", estCost);
                    map.put("reason", mv.getNotes());
                    map.put("date", mv.getDate().toLocalDate().toString());
                    dataList.add(map);
                }
            }
        }

        List<FinishedProductMovement> fpMovements = finishedProductMovementRepository.findAll();
        for (FinishedProductMovement mv : fpMovements) {
            if ((mv.getType() == FinishedProductMovementType.ADJUSTMENT || mv.getType() == FinishedProductMovementType.RECALL) && mv.getNotes() != null) {
                String notes = mv.getNotes().toLowerCase();
                if (notes.contains("waste") || notes.contains("expired") || notes.contains("scrap") || notes.contains("write-off") || notes.contains("written-off")) {
                    if (mv.getDate().isBefore(start) || mv.getDate().isAfter(end)) continue;
                    BigDecimal price = mv.getFinishedProduct().getUnitCost() != null ? mv.getFinishedProduct().getUnitCost() : BigDecimal.ZERO;
                    BigDecimal estCost = mv.getQuantity().multiply(price);
                    totalWastageQuantity = totalWastageQuantity.add(mv.getQuantity());

                    String name = mv.getFinishedProduct().getProductName();
                    wastageByProduct.put(name, wastageByProduct.getOrDefault(name, BigDecimal.ZERO).add(mv.getQuantity()));

                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("type", "PRODUCT");
                    map.put("id", mv.getId());
                    map.put("name", name);
                    map.put("quantity", mv.getQuantity());
                    map.put("unit", "Bottles");
                    map.put("costEstimate", estCost);
                    map.put("reason", mv.getNotes());
                    map.put("date", mv.getDate().toLocalDate().toString());
                    dataList.add(map);
                }
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalWastageQuantity", totalWastageQuantity);
        summary.put("totalWastageCost", totalWastageCost);
        summary.put("wastageByProduct", wastageByProduct);
        summary.put("wastageByMaterial", wastageByMaterial);
        summary.put("comparisonPeriodWastageCost", comparisonPeriodWastageCost);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("summary", summary);
        report.put("data", dataList);
        return report;
    }

    private BigDecimal calculateWastageCostInPeriod(OffsetDateTime start, OffsetDateTime end) {
        BigDecimal cost = BigDecimal.ZERO;
        List<StockMovement> rmMovements = stockMovementRepository.findAll();
        for (StockMovement mv : rmMovements) {
            if (mv.getType() == StockMovementType.STOCK_OUT && mv.getNotes() != null) {
                String notes = mv.getNotes().toLowerCase();
                if (notes.contains("waste") || notes.contains("expired") || notes.contains("scrap") || notes.contains("write-off") || notes.contains("written-off")) {
                    if (start != null && mv.getDate().isBefore(start)) continue;
                    if (end != null && mv.getDate().isAfter(end)) continue;
                    BigDecimal price = mv.getRawMaterial().getCostPerUnit() != null ? mv.getRawMaterial().getCostPerUnit() : BigDecimal.ZERO;
                    cost = cost.add(mv.getQuantity().multiply(price));
                }
            }
        }

        List<FinishedProductMovement> fpMovements = finishedProductMovementRepository.findAll();
        for (FinishedProductMovement mv : fpMovements) {
            if ((mv.getType() == FinishedProductMovementType.ADJUSTMENT || mv.getType() == FinishedProductMovementType.RECALL) && mv.getNotes() != null) {
                String notes = mv.getNotes().toLowerCase();
                if (notes.contains("waste") || notes.contains("expired") || notes.contains("scrap") || notes.contains("write-off") || notes.contains("written-off")) {
                    if (start != null && mv.getDate().isBefore(start)) continue;
                    if (end != null && mv.getDate().isAfter(end)) continue;
                    BigDecimal price = mv.getFinishedProduct().getUnitCost() != null ? mv.getFinishedProduct().getUnitCost() : BigDecimal.ZERO;
                    cost = cost.add(mv.getQuantity().multiply(price));
                }
            }
        }
        return cost;
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public Page<Map<String, Object>> getScheduledReports(Pageable pageable) {
        return scheduledReportRepository.findAll(pageable).map(this::mapToScheduledReportMap);
    }

    public Map<String, Object> createScheduledReport(Map<String, Object> body, String username) {
        String reportType = (String) body.get("reportType");
        String freqStr = (String) body.get("frequency");
        Object rec = body.get("recipients");
        String recipients = "";
        if (rec instanceof List) {
            recipients = ((List<?>) rec).stream().map(Object::toString).collect(Collectors.joining(","));
        } else if (rec instanceof String) {
            recipients = (String) rec;
        }

        String timeStr = (String) body.get("deliveryTime");
        LocalTime deliveryTime = LocalTime.NOON;
        if (timeStr != null && !timeStr.isEmpty()) {
            try {
                deliveryTime = LocalTime.parse(timeStr);
            } catch (Exception e) {
                try {
                    deliveryTime = LocalTime.parse(timeStr + ":00");
                } catch (Exception ex) {}
            }
        }

        Boolean isActive = (Boolean) body.getOrDefault("isActive", true);
        User user = userRepository.findByEmail(username).orElse(null);

        ScheduledReport report = ScheduledReport.builder()
                .createdByUser(user)
                .reportType(reportType)
                .frequency(freqStr != null ? ReportFrequency.valueOf(freqStr.toUpperCase()) : ReportFrequency.DAILY)
                .recipients(recipients)
                .deliveryTime(deliveryTime)
                .isActive(isActive)
                .build();

        ScheduledReport saved = scheduledReportRepository.save(report);
        return mapToScheduledReportMap(saved);
    }

    public Map<String, Object> updateScheduledReport(UUID id, Map<String, Object> body) {
        ScheduledReport report = scheduledReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Scheduled report not found"));

        if (body.containsKey("reportType")) {
            report.setReportType((String) body.get("reportType"));
        }
        if (body.containsKey("frequency")) {
            String freqStr = (String) body.get("frequency");
            report.setFrequency(freqStr != null ? ReportFrequency.valueOf(freqStr.toUpperCase()) : null);
        }
        if (body.containsKey("recipients")) {
            Object rec = body.get("recipients");
            if (rec instanceof List) {
                report.setRecipients(((List<?>) rec).stream().map(Object::toString).collect(Collectors.joining(",")));
            } else if (rec instanceof String) {
                report.setRecipients((String) rec);
            }
        }
        if (body.containsKey("deliveryTime")) {
            String timeStr = (String) body.get("deliveryTime");
            if (timeStr != null && !timeStr.isEmpty()) {
                try {
                    report.setDeliveryTime(LocalTime.parse(timeStr));
                } catch (Exception e) {
                    try {
                        report.setDeliveryTime(LocalTime.parse(timeStr + ":00"));
                    } catch (Exception ex) {}
                }
            }
        }
        if (body.containsKey("isActive")) {
            report.setIsActive((Boolean) body.get("isActive"));
        }

        ScheduledReport saved = scheduledReportRepository.save(report);
        return mapToScheduledReportMap(saved);
    }

    public void deleteScheduledReport(UUID id) {
        scheduledReportRepository.deleteById(id);
    }

    private Map<String, Object> mapToScheduledReportMap(ScheduledReport report) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", report.getId());
        map.put("reportType", report.getReportType());
        map.put("frequency", report.getFrequency() != null ? report.getFrequency().name() : null);
        map.put("recipients", report.getRecipients());
        map.put("deliveryTime", report.getDeliveryTime() != null ? report.getDeliveryTime().toString() : null);
        map.put("isActive", report.getIsActive());
        map.put("lastSentAt", report.getLastSentAt() != null ? report.getLastSentAt().toString() : null);
        map.put("createdAt", report.getCreatedAt() != null ? report.getCreatedAt().toString() : null);
        map.put("createdBy", report.getCreatedByUser() != null ? report.getCreatedByUser().getFullName() : null);
        return map;
    }
}
