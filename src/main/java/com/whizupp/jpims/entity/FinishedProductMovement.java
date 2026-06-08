package com.whizupp.jpims.entity;

import com.whizupp.jpims.enums.DomainEnums.FinishedProductMovementType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "finished_product_movements")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinishedProductMovement extends BaseAuditEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "finished_product_id")
    private FinishedProduct finishedProduct;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    private User recordedBy;

    @Enumerated(EnumType.STRING)
    private FinishedProductMovementType type;

    private BigDecimal quantity;
    private OffsetDateTime date;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private UUID referenceId;
}
