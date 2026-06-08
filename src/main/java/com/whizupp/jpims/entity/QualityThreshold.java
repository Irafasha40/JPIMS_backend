package com.whizupp.jpims.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "quality_thresholds")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityThreshold extends BaseAuditEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String productName;

    private BigDecimal phMin;
    private BigDecimal phMax;
    private BigDecimal brixMin;
    private BigDecimal brixMax;

    @Builder.Default
    private Boolean isDefault = false;
}
