package com.whizupp.jpims.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
public class PurchaseOrderReceiveRequest {
    /** Optional note stored on stock movements for this receipt (e.g. short shipment, expected follow-up). */
    private String notes;

    @Valid
    private List<ItemReceive> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemReceive {
        @NotNull
        private UUID itemId;

        @NotNull
        @DecimalMin(value = "0.0")
        private java.math.BigDecimal receivedQuantity;
    }
}
