package com.whizupp.jpims.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotNull(message = "Base quantity is required")
    @Positive(message = "Base quantity must be positive")
    private BigDecimal baseQuantity;

    private String status;
    private String notes;

    @NotEmpty(message = "Ingredients are required")
    @Valid
    private List<RecipeIngredientRequest> ingredients;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipeIngredientRequest {
        @NotNull(message = "Raw material ID is required")
        private UUID materialId;

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private BigDecimal quantity;
    }
}
