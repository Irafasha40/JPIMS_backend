package com.whizupp.jpims.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "batch_ingredients")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchIngredient extends BaseAuditEntity {
    @Id
    @GeneratedValue
    private UUID id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private ProductionBatch productionBatch;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "raw_material_id")
    private RawMaterial rawMaterial;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityRequired;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityIssued;

    @Builder.Default
    private Boolean isIssued = false;
}
