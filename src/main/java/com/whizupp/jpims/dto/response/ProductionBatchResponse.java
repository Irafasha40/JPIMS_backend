package com.whizupp.jpims.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
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
public class ProductionBatchResponse {
    private UUID id;
    private String batchNumber;
    private String productName;
    private BigDecimal targetQuantity;
    private BigDecimal actualYield;
    private BigDecimal loss;
    private String lossReason;
    private String status;
    private LocalDate productionDate;
    private UUID recipeId;
    private String recipeName;
    private Integer shelfLifeDays;
    private String assignedTo;
    private Boolean finishedGoodsTransferred;
    private Boolean stockApproved;
    private List<IngredientLine> ingredients;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientLine {
        private UUID materialId;
        private String materialName;
        private BigDecimal quantityRequired;
        private BigDecimal quantityIssued;
        private String unitOfMeasure;
        private Boolean issued;
    }
}
