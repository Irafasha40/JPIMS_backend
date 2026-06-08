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
public class FinishedProductResponse {
    private UUID id;
    private String productName;
    private String flavor;
    private String packagingSize;
    private String lotNumber;
    private BigDecimal quantity;
    private BigDecimal volumeLiters;
    private Integer bottlesUsed;
    private Integer boxesUsed;
    private LocalDate expiryDate;
    private String storageLocation;
    private String status;
    private BigDecimal unitCost;
    private UUID batchId;
    private String batchNumber;
}
