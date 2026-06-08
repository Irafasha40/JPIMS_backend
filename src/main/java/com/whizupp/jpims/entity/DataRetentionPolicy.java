package com.whizupp.jpims.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "data_retention_policies")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataRetentionPolicy extends BaseAuditEntity {
    @Id
    @GeneratedValue
    private UUID id;

    private String module;
    private Integer retentionDays;

    @Builder.Default
    private Boolean archiveEnabled = false;

    private OffsetDateTime lastAppliedAt;
}
