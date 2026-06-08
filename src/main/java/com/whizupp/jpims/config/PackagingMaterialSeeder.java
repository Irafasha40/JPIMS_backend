package com.whizupp.jpims.config;

import com.whizupp.jpims.entity.RawMaterial;
import com.whizupp.jpims.repository.RawMaterialRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(20)
@RequiredArgsConstructor
public class PackagingMaterialSeeder implements CommandLineRunner {
    private final RawMaterialRepository rawMaterialRepository;

    @Value("${app.packaging.bottle-material-name:Empty Bottle (1L)}")
    private String bottleName;

    @Value("${app.packaging.box-material-name:Carton Box (12 bottles)}")
    private String boxName;

    @Value("${app.packaging.seed-stock:10000}")
    private BigDecimal seedStock;

    @Override
    public void run(String... args) {
        ensureMaterial(bottleName, "PACKAGING", "pcs");
        ensureMaterial(boxName, "PACKAGING", "pcs");
    }

    private void ensureMaterial(String name, String category, String unit) {
        if (rawMaterialRepository.findFirstByNameIgnoreCase(name).isPresent()) {
            return;
        }
        RawMaterial material = RawMaterial.builder()
                .name(name)
                .category(category)
                .unitOfMeasure(unit)
                .currentStock(seedStock)
                .minimumThreshold(BigDecimal.valueOf(500))
                .costPerUnit(BigDecimal.ONE)
                .isActive(true)
                .build();
        rawMaterialRepository.save(material);
        log.info("Seeded packaging material: {}", name);
    }
}
