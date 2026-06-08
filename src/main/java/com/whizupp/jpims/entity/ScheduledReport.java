package com.whizupp.jpims.entity;

import com.whizupp.jpims.enums.DomainEnums.ReportFrequency;
import jakarta.persistence.*;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "scheduled_reports")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledReport extends BaseAuditEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    private String reportType;

    @Enumerated(EnumType.STRING)
    private ReportFrequency frequency;

    @Column(columnDefinition = "TEXT")
    private String recipients;

    private LocalTime deliveryTime;

    @Builder.Default
    private Boolean isActive = true;

    private OffsetDateTime lastSentAt;
}
