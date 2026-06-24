package com.whizupp.jpims.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "raw_materials")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawMaterial extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String name;
    private String category;
    private String unitOfMeasure;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal currentStock;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal minimumThreshold;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal costPerUnit;

    @Builder.Default
    private Boolean isActive = true;

    private LocalDate expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Supplier supplier;
}
