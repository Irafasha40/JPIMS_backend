package com.whizupp.jpims.dto.response;

import com.whizupp.jpims.enums.DomainEnums.PurchaseOrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** List view for purchase orders — avoids Hibernate proxy JSON serialization issues. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderSummaryResponse {
    private UUID id;
    private String poNumber;
    private String supplierName;
    private PurchaseOrderStatus status;
    private LocalDate expectedDeliveryDate;
    private LocalDate actualDeliveryDate;
    private BigDecimal totalCost;
    private OffsetDateTime createdAt;
}
