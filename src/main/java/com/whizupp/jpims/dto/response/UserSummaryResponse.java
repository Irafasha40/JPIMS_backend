package com.whizupp.jpims.dto.response;

import com.whizupp.jpims.enums.DomainEnums.Role;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String phone;
    private String employeeId;
    private Role role;
    private String department;
}
