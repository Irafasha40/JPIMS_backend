package com.whizupp.jpims.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String phone;
    private String employeeId;
    private String department;
    private String role;
    private Boolean isActive;
    private Boolean isLocked;
    private OffsetDateTime lastLogin;
    private Boolean mfaEnabled;
    private Boolean emailVerified;
}
