package com.whizupp.jpims.entity;

import com.whizupp.jpims.enums.DomainEnums.AccessRequestStatus;
import com.whizupp.jpims.enums.DomainEnums.Role;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "access_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessRequest extends BaseAuditEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private User requester;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role requestedRole;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessRequestStatus status;

    private UUID reviewedBy;
    private OffsetDateTime reviewedAt;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;
}
