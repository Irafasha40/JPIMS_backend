package com.whizupp.jpims.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "customers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends BaseAuditEntity {
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

    @Builder.Default
    private Boolean isActive = true;
}
