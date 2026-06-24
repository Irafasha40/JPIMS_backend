package com.whizupp.jpims.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class RawMaterialRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String category;

    @NotBlank(message = "Unit of measure is required")
    private String unitOfMeasure;

    @NotNull(message = "Current stock is required")
    @Positive(message = "Current stock must be positive")
    private BigDecimal currentStock;

    @NotNull(message = "Minimum threshold is required")
    private BigDecimal minimumThreshold;

    @NotNull(message = "Cost per unit is required")
    @Positive(message = "Cost per unit must be positive")
    private BigDecimal costPerUnit;

    private UUID supplierId;
    private LocalDate expiryDate;
}
