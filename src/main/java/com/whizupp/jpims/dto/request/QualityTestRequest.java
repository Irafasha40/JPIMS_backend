package com.whizupp.jpims.dto.request;

import jakarta.validation.constraints.NotNull;
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
public class QualityTestRequest {
    @NotNull(message = "Batch ID is required")
    private UUID batchId;

    @NotNull(message = "pH level is required")
    private BigDecimal phLevel;

    @NotNull(message = "Brix level is required")
    private BigDecimal brixLevel;

    private String appearance;
    private String color;
    private String taste;
    private String notes;
}
