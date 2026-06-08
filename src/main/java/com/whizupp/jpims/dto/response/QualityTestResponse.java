package com.whizupp.jpims.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityTestResponse {
    private UUID id;
    private UUID batchId;
    private String batchNumber;
    private String productName;
    private BigDecimal phLevel;
    private BigDecimal brixLevel;
    private String appearance;
    private String color;
    private String taste;
    private String result;
    private String testedBy;
    private OffsetDateTime testDate;
    private String notes;
}
