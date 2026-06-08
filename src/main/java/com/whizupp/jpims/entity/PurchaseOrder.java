package com.whizupp.jpims.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.whizupp.jpims.enums.DomainEnums.PurchaseOrderStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "purchase_orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder extends BaseAuditEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Supplier supplier;

    @Column(nullable = false, unique = true)
    private String poNumber;

    @Enumerated(EnumType.STRING)
    private PurchaseOrderStatus status;

    private LocalDate expectedDeliveryDate;
    private LocalDate actualDeliveryDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private BigDecimal totalCost;
}
