package com.whizupp.jpims.dto.response;

import com.whizupp.jpims.enums.DomainEnums.PurchaseOrderStatus;
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
public class PurchaseOrderDetailResponse {
    private UUID id;
    private String poNumber;
    private String supplierName;
    private PurchaseOrderStatus status;
    private LocalDate expectedDeliveryDate;
    private LocalDate actualDeliveryDate;
    private String notes;
    private BigDecimal totalCost;
    private List<ItemLine> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemLine {
        private UUID id;
        private UUID materialId;
        private String materialName;
        private String unitOfMeasure;
        private BigDecimal quantity;
        private BigDecimal unitCost;
        private BigDecimal receivedQuantity;
    }
}
