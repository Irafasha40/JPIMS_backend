package com.whizupp.jpims.dto.response;

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
public class RecipeResponse {
    private UUID id;
    private String name;
    private String productName;
    private BigDecimal baseQuantity;
    private String status;
    private String notes;
    private List<RecipeIngredientResponse> ingredients;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipeIngredientResponse {
        private UUID materialId;
        private String materialName;
        private BigDecimal quantity;
        private String unitOfMeasure;
    }
}
