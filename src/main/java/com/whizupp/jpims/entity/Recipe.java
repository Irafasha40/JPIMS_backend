package com.whizupp.jpims.entity;

import com.whizupp.jpims.enums.DomainEnums.RecipeStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "recipes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recipe extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String productName;
    private Integer version;
    private BigDecimal baseQuantity;

    @Enumerated(EnumType.STRING)
    private RecipeStatus status;

    private BigDecimal costPerBatch;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private UUID approvedBy;
    private OffsetDateTime approvedAt;

    private BigDecimal calories;
    private BigDecimal sugarContent;
    private BigDecimal vitaminC;

    private Integer shelfLifeDays;
}
