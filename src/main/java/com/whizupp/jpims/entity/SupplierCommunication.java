package com.whizupp.jpims.entity;

import com.whizupp.jpims.enums.DomainEnums.SupplierCommunicationType;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "supplier_communications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierCommunication extends BaseAuditEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "logged_by")
    private User loggedBy;

    @Enumerated(EnumType.STRING)
    private SupplierCommunicationType type;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDate followUpDate;
    private OffsetDateTime communicationDate;
}
