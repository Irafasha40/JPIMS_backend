package com.whizupp.jpims.service;

import com.whizupp.jpims.entity.FinishedProduct;
import com.whizupp.jpims.entity.ProductCatalogItem;
import com.whizupp.jpims.exception.InvalidOperationException;
import com.whizupp.jpims.exception.ResourceNotFoundException;
import com.whizupp.jpims.repository.FinishedProductRepository;
import com.whizupp.jpims.repository.ProductCatalogRepository;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
public class ProductCatalogService {

    private final ProductCatalogRepository repository;
    private final FinishedProductRepository finishedProductRepository;

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> list(Pageable pageable) {
        return repository.findAllByOrderByProductNameAsc(pageable).map(this::toMap);
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> body) {
        String name = getString(body, "productName");
        if (name == null || name.isBlank()) {
            throw new InvalidOperationException("Product name is required");
        }
        if (repository.existsByProductNameIgnoreCase(name.trim())) {
            throw new InvalidOperationException("A catalog entry already exists for: " + name.trim());
        }
        BigDecimal cost = getBigDecimal(body, "unitCost");
        if (cost == null || cost.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidOperationException("Unit cost must be a positive number");
        }
        String description = getString(body, "description");

        ProductCatalogItem item = ProductCatalogItem.builder()
                .productName(name.trim())
                .unitCost(cost)
                .description(description != null ? description.trim() : null)
                .build();

        ProductCatalogItem saved = repository.save(item);
        log.info("Product catalog entry created: {} @ {}", saved.getProductName(), saved.getUnitCost());
        return toMap(saved);
    }

    @Transactional
    public Map<String, Object> update(UUID id, Map<String, Object> body) {
        ProductCatalogItem item = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catalog entry not found"));

        String name = getString(body, "productName");
        if (name != null && !name.isBlank() && !name.trim().equalsIgnoreCase(item.getProductName())) {
            if (repository.existsByProductNameIgnoreCase(name.trim())) {
                throw new InvalidOperationException("A catalog entry already exists for: " + name.trim());
            }
            item.setProductName(name.trim());
        }

        BigDecimal cost = getBigDecimal(body, "unitCost");
        if (cost != null) {
            if (cost.compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidOperationException("Unit cost must be a positive number");
            }
            item.setUnitCost(cost);
        }

        String description = getString(body, "description");
        if (description != null) {
            item.setDescription(description.trim().isEmpty() ? null : description.trim());
        }

        return toMap(repository.save(item));
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Catalog entry not found");
        }
        repository.deleteById(id);
        log.info("Product catalog entry deleted: {}", id);
    }

    /**
     * Called internally by FinishedProductService to auto-apply cost.
     */
    @Transactional(readOnly = true)
    public Optional<BigDecimal> findCostByProductName(String productName) {
        if (productName == null || productName.isBlank()) return Optional.empty();
        return repository.findByProductNameIgnoreCase(productName.trim())
                .map(ProductCatalogItem::getUnitCost);
    }

    /**
     * Applies catalog unit costs to all existing finished products that match a catalog entry.
     * Updates ALL matching finished products regardless of whether they already have a cost
     * (so price changes in the catalog can be propagated).
     * Returns count of updated records.
     */
    @Transactional
    public Map<String, Object> syncPricesToFinishedProducts() {
        List<FinishedProduct> allProducts = finishedProductRepository.findAll();
        int updated = 0;
        int skipped = 0;
        for (FinishedProduct fp : allProducts) {
            if (fp.getProductName() == null) continue;
            Optional<ProductCatalogItem> catalogItem =
                    repository.findByProductNameIgnoreCase(fp.getProductName().trim());
            if (catalogItem.isPresent()) {
                fp.setUnitCost(catalogItem.get().getUnitCost());
                finishedProductRepository.save(fp);
                updated++;
            } else {
                skipped++;
            }
        }
        log.info("Sync prices: {} finished products updated, {} had no catalog match", updated, skipped);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("updated", updated);
        result.put("skipped", skipped);
        result.put("message", updated + " product(s) updated with catalog prices.");
        return result;
    }

    private Map<String, Object> toMap(ProductCatalogItem item) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", item.getId());
        map.put("productName", item.getProductName());
        map.put("unitCost", item.getUnitCost());
        map.put("description", item.getDescription());
        map.put("createdAt", item.getCreatedAt() != null ? item.getCreatedAt().toLocalDate().toString() : null);
        map.put("updatedAt", item.getUpdatedAt() != null ? item.getUpdatedAt().toLocalDate().toString() : null);
        return map;
    }

    private String getString(Map<String, Object> body, String key) {
        Object val = body.get(key);
        return val != null ? val.toString() : null;
    }

    private BigDecimal getBigDecimal(Map<String, Object> body, String key) {
        Object val = body.get(key);
        if (val == null) return null;
        try {
            return new BigDecimal(val.toString());
        } catch (NumberFormatException e) {
            throw new InvalidOperationException("Invalid number for " + key + ": " + val);
        }
    }
}
