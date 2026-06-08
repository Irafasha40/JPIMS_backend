package com.whizupp.jpims.service;

import com.whizupp.jpims.dto.request.FirstLoginPasswordRequest;
import com.whizupp.jpims.dto.request.ChangePasswordRequest;
import com.whizupp.jpims.dto.request.ConfirmMfaRequest;
import com.whizupp.jpims.dto.request.DisableMfaRequest;
import com.whizupp.jpims.dto.request.ForgotPasswordRequest;
import com.whizupp.jpims.dto.request.LoginRequest;
import com.whizupp.jpims.dto.request.RefreshTokenRequest;
import com.whizupp.jpims.dto.request.RegisterRequest;
import com.whizupp.jpims.dto.request.ResetPasswordRequest;
import com.whizupp.jpims.dto.request.UpdateProfileRequest;
import com.whizupp.jpims.dto.request.VerifyMfaRequest;
import com.whizupp.jpims.dto.response.AuthResponse;
import com.whizupp.jpims.dto.response.MessageResponse;
import com.whizupp.jpims.dto.response.MfaSetupResponse;
import com.whizupp.jpims.dto.response.UserSummaryResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse completeFirstLogin(FirstLoginPasswordRequest request);

    AuthResponse verifyMfa(VerifyMfaRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    MessageResponse verifyEmail(String token);

    MessageResponse forgotPassword(ForgotPasswordRequest request);

    MessageResponse resetPassword(ResetPasswordRequest request);

    MfaSetupResponse setupMfa(String email);

    MessageResponse confirmMfa(String email, ConfirmMfaRequest request);

    MessageResponse disableMfa(String email, DisableMfaRequest request);

    UserSummaryResponse getMe(String email);

    UserSummaryResponse updateMe(String email, UpdateProfileRequest request);

    MessageResponse changePassword(String email, ChangePasswordRequest request);

    MessageResponse logout();
}
