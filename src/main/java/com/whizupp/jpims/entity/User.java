package com.whizupp.jpims.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.whizupp.jpims.enums.DomainEnums.Role;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String fullName;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    @JsonIgnore
    private String password;
    private String phone;
    private String employeeId;
    private String department;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    @Builder.Default
    private Boolean isActive = true;
    @Builder.Default
    private Boolean isLocked = false;
    @Builder.Default
    private Integer loginAttempts = 0;
    private OffsetDateTime lastLogin;
    @Builder.Default
    private Boolean mfaEnabled = false;
    @JsonIgnore
    private String mfaSecret;

    @Builder.Default
    private Boolean emailVerified = false;
    /** When true, user must set a new password (e.g. admin-created account) before receiving full tokens. */
    @Builder.Default
    private Boolean mustChangePassword = false;
    private String emailVerificationToken;
    private OffsetDateTime emailVerificationExpiry;
    private String passwordResetToken;
    private OffsetDateTime passwordResetExpiry;
}
