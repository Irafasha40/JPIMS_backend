package com.whizupp.jpims.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "recipe_versions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeVersion extends BaseAuditEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private User changedBy;

    private Integer version;
    private String changeDescription;
    private OffsetDateTime changedAt;

    @Column(columnDefinition = "TEXT")
    private String snapshotJson;
}
