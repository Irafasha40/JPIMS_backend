package com.whizupp.jpims.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class FinishedProductRequest {
    @NotBlank(message = "Product name is required")
    private String productName;

    private String flavor;
    private String packagingSize;

    @NotNull(message = "Quantity is required")
    private BigDecimal quantity;

    private LocalDate expiryDate;
    private String storageLocation;
    private BigDecimal unitCost;
    private String status;

    // For transfer from batch
    private UUID batchId;
    private String lotNumber;
}
