package com.whizupp.jpims.entity;

import com.whizupp.jpims.enums.DomainEnums.BatchStatus;
import com.whizupp.jpims.entity.Recipe;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "production_batches")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionBatch extends BaseAuditEntity {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(nullable = false, unique = true)
    private String batchNumber;
    @Column(nullable = false)
    private String productName;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal targetQuantity;
    @Column(precision = 19, scale = 4)
    private BigDecimal actualYield;
    @Column(precision = 19, scale = 4)
    private BigDecimal loss;
    private String lossReason;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatchStatus status;
    private LocalDate productionDate;
    private OffsetDateTime startTime;
    private OffsetDateTime completionTime;
    @Builder.Default
    private Boolean stockApproved = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;
}
