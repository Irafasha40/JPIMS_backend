package com.whizupp.jpims.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
public class PurchaseOrderCreateRequest {
    @NotNull
    private UUID supplierId;

    @NotNull
    private LocalDate expectedDeliveryDate;

    private String notes;

    @Valid
    @NotNull
    private List<Item> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        @NotNull
        private UUID materialId;

        @NotNull
        @DecimalMin(value = "0.0001")
        private java.math.BigDecimal quantity;

        @NotNull
        @DecimalMin(value = "0.0001")
        private java.math.BigDecimal unitCost;
    }
}
