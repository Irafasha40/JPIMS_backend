package com.whizupp.jpims.entity;

import com.whizupp.jpims.enums.DomainEnums.Appearance;
import com.whizupp.jpims.enums.DomainEnums.ColorStatus;
import com.whizupp.jpims.enums.DomainEnums.TasteStatus;
import com.whizupp.jpims.enums.DomainEnums.TestResult;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "quality_tests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityTest extends BaseAuditEntity {
    @Id
    @GeneratedValue
    private UUID id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private ProductionBatch productionBatch;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tested_by")
    private User testedBy;
    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal phLevel;
    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal brixLevel;
    @Enumerated(EnumType.STRING)
    private Appearance appearance;
    @Enumerated(EnumType.STRING)
    private ColorStatus color;
    @Enumerated(EnumType.STRING)
    private TasteStatus taste;
    @Enumerated(EnumType.STRING)
    private TestResult result;
    private String notes;
    private OffsetDateTime testDate;
    private String certificateNumber;
}
