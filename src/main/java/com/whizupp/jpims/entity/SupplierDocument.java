package com.whizupp.jpims.entity;

import com.whizupp.jpims.enums.DomainEnums.SupplierDocumentType;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "supplier_documents")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDocument extends BaseAuditEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @Enumerated(EnumType.STRING)
    private SupplierDocumentType documentType;

    private String fileName;

    @Column(columnDefinition = "TEXT")
    private String filePath;

    private LocalDate expiryDate;
    private OffsetDateTime uploadedAt;
}
