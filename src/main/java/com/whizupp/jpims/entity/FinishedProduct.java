package com.whizupp.jpims.entity;

import com.whizupp.jpims.enums.DomainEnums.FinishedProductStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "finished_products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinishedProduct extends BaseAuditEntity {
    @Id
    @GeneratedValue
    private UUID id;
    private String productName;
    private String flavor;
    private String packagingSize;
    private String lotNumber;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;
    private LocalDate expiryDate;
    private String storageLocation;
    @Enumerated(EnumType.STRING)
    private FinishedProductStatus status;
    @Column(precision = 19, scale = 4)
    private BigDecimal unitCost;
    @Column(precision = 19, scale = 4)
    private BigDecimal volumeLiters;
    private Integer bottlesUsed;
    private Integer boxesUsed;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private ProductionBatch productionBatch;
}
