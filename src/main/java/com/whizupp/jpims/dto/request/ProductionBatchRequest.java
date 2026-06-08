package com.whizupp.jpims.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionBatchRequest {
    @NotNull(message = "Recipe ID is required")
    private UUID recipeId;

    @NotNull(message = "Target quantity is required")
    @Positive(message = "Target quantity must be positive")
    private BigDecimal targetQuantity;

    private String notes;
}
