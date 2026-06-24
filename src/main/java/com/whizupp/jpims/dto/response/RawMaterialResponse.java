package com.whizupp.jpims.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawMaterialResponse {
    private UUID id;
    private String name;
    private String category;
    private String unitOfMeasure;
    private BigDecimal currentStock;
    private BigDecimal minimumThreshold;
    private BigDecimal costPerUnit;
    private Boolean isActive;
    private LocalDate expiryDate;
    private SupplierResponse supplier;
}
