package com.whizupp.jpims.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private Boolean requiresMfa;
    /** When true, client must collect a new password and call {@code POST /api/auth/complete-first-login} with {@link #tempToken}. */
    private Boolean requiresPasswordChange;
    private String tempToken;
    private UserSummaryResponse user;
    private String message;
}
