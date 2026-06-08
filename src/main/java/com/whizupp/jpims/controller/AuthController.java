package com.whizupp.jpims.controller;

import com.whizupp.jpims.dto.request.ChangePasswordRequest;
import com.whizupp.jpims.dto.request.ConfirmMfaRequest;
import com.whizupp.jpims.dto.request.DisableMfaRequest;
import com.whizupp.jpims.dto.request.ForgotPasswordRequest;
import com.whizupp.jpims.dto.request.FirstLoginPasswordRequest;
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
import com.whizupp.jpims.exception.MfaRequiredException;
import com.whizupp.jpims.exception.PasswordChangeRequiredException;
import com.whizupp.jpims.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (MfaRequiredException ex) {
            return ResponseEntity.ok(AuthResponse.builder()
                    .requiresMfa(true)
                    .tempToken(ex.getMessage())
                    .build());
        } catch (PasswordChangeRequiredException ex) {
            return ResponseEntity.ok(AuthResponse.builder()
                    .requiresPasswordChange(true)
                    .tempToken(ex.getMessage())
                    .build());
        }
    }

    @PostMapping("/complete-first-login")
    public ResponseEntity<AuthResponse> completeFirstLogin(@Valid @RequestBody FirstLoginPasswordRequest request) {
        return ResponseEntity.ok(authService.completeFirstLogin(request));
    }

    @PostMapping("/verify-mfa")
    public ResponseEntity<AuthResponse> verifyMfa(@Valid @RequestBody VerifyMfaRequest request) {
        return ResponseEntity.ok(authService.verifyMfa(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
        return ResponseEntity.ok(authService.verifyEmail(token));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @PostMapping("/setup-mfa")
    public ResponseEntity<MfaSetupResponse> setupMfa(Authentication authentication) {
        return ResponseEntity.ok(authService.setupMfa(authentication.getName()));
    }

    @PostMapping("/confirm-mfa")
    public ResponseEntity<MessageResponse> confirmMfa(Authentication authentication, @Valid @RequestBody ConfirmMfaRequest request) {
        return ResponseEntity.ok(authService.confirmMfa(authentication.getName(), request));
    }

    @DeleteMapping("/disable-mfa")
    public ResponseEntity<MessageResponse> disableMfa(Authentication authentication, @Valid @RequestBody DisableMfaRequest request) {
        return ResponseEntity.ok(authService.disableMfa(authentication.getName(), request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserSummaryResponse> me(Authentication authentication) {
        return ResponseEntity.ok(authService.getMe(authentication.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserSummaryResponse> updateMe(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(authService.updateMe(authentication.getName(), request));
    }

    @PutMapping("/me/change-password")
    public ResponseEntity<MessageResponse> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(authService.changePassword(authentication.getName(), request));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout() {
        log.debug("Logout endpoint invoked");
        return ResponseEntity.ok(authService.logout());
    }
}
