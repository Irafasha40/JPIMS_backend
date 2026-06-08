package com.whizupp.jpims.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.whizupp.jpims.enums.DomainEnums.StockMovementType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "stock_movements")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement extends BaseAuditEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "raw_material_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private RawMaterial rawMaterial;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User recordedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_batch_id")
    @JsonIgnore
    private ProductionBatch productionBatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    @JsonIgnore
    private PurchaseOrder purchaseOrder;

    @Enumerated(EnumType.STRING)
    private StockMovementType type;

    private BigDecimal quantity;
    private String referenceNumber;
    private OffsetDateTime date;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
