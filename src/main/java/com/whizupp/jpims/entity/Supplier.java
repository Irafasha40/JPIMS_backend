package com.whizupp.jpims.entity;

import com.whizupp.jpims.enums.DomainEnums.SupplierOnboardingStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Supplier extends BaseAuditEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String contactPerson;
    private String phone;
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String paymentTerms;
    private Integer leadTimeDays;
    private BigDecimal rating;

    @Builder.Default
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    private SupplierOnboardingStatus onboardingStatus;
}
