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
import com.whizupp.jpims.entity.User;
import com.whizupp.jpims.exception.AccountLockedException;
import com.whizupp.jpims.exception.InvalidOperationException;
import com.whizupp.jpims.exception.InvalidTokenException;
import com.whizupp.jpims.exception.MfaRequiredException;
import com.whizupp.jpims.exception.PasswordChangeRequiredException;
import com.whizupp.jpims.exception.ResourceNotFoundException;
import com.whizupp.jpims.repository.UserRepository;
import com.whizupp.jpims.security.AppUserDetails;
import com.whizupp.jpims.security.CustomUserDetailsService;
import com.whizupp.jpims.security.JwtUtil;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    @Value("${app.auth.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${app.auth.password-reset-expiry-minutes:60}")
    private int passwordResetExpiryMinutes;

    @Value("${app.auth.email-verification-expiry-hours:24}")
    private int emailVerificationExpiryHours;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            throw new InvalidOperationException("Email already exists");
        });

        String verificationToken = UUID.randomUUID().toString();
        User saved = userRepository.save(User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .employeeId(request.getEmployeeId())
                .department(request.getDepartment())
                .role(request.getRole())
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .emailVerified(false)
                .emailVerificationToken(verificationToken)
                .emailVerificationExpiry(OffsetDateTime.now().plusHours(emailVerificationExpiryHours))
                .build());

        log.info("User registered: {}", saved.getEmail());
        return AuthResponse.builder()
                .user(mapUser(saved))
                .message("Verification email sent")
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid credentials"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new InvalidOperationException("Account is inactive");
        }
        if (Boolean.TRUE.equals(user.getIsLocked())) {
            throw new AccountLockedException("Account is locked due to failed login attempts");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            int attempts = user.getLoginAttempts() == null ? 0 : user.getLoginAttempts();
            attempts++;
            user.setLoginAttempts(attempts);
            if (attempts >= maxLoginAttempts) {
                user.setIsLocked(true);
                userRepository.save(user);
                throw new AccountLockedException("Account locked after maximum failed attempts");
            }
            userRepository.save(user);
            throw new ResourceNotFoundException("Invalid credentials");
        }

        user.setLoginAttempts(0);
        userRepository.save(user);

        AppUserDetails userDetails = (AppUserDetails) customUserDetailsService.loadUserByUsername(user.getEmail());

        if (Boolean.TRUE.equals(user.getMustChangePassword())) {
            String tempToken = jwtUtil.generatePasswordChangeRequiredToken(userDetails);
            throw new PasswordChangeRequiredException(tempToken);
        }

        user.setLastLogin(OffsetDateTime.now());
        userRepository.save(user);

        if (Boolean.TRUE.equals(user.getMfaEnabled())) {
            String tempToken = jwtUtil.generateToken(userDetails);
            throw new MfaRequiredException(tempToken);
        }

        return AuthResponse.builder()
                .accessToken(jwtUtil.generateToken(userDetails))
                .refreshToken(jwtUtil.generateRefreshToken(userDetails))
                .requiresMfa(false)
                .user(mapUser(user))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse completeFirstLogin(FirstLoginPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidOperationException("Passwords do not match");
        }
        if (!request.getNewPassword().matches("^(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$")) {
            throw new InvalidOperationException("Password must be minimum 8 characters with at least one number and one special character");
        }

        String email = jwtUtil.extractEmail(request.getTempToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        AppUserDetails userDetails = (AppUserDetails) customUserDetailsService.loadUserByUsername(email);

        if (!jwtUtil.validateToken(request.getTempToken(), userDetails)) {
            throw new InvalidTokenException("Invalid or expired session");
        }
        if (!JwtUtil.TOKEN_TYPE_PASSWORD_CHANGE_REQUIRED.equals(jwtUtil.extractTokenType(request.getTempToken()))) {
            throw new InvalidTokenException("Invalid session type");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        user.setLastLogin(OffsetDateTime.now());
        userRepository.save(user);

        return AuthResponse.builder()
                .accessToken(jwtUtil.generateToken(userDetails))
                .refreshToken(jwtUtil.generateRefreshToken(userDetails))
                .requiresMfa(false)
                .user(mapUser(user))
                .build();
    }

    @Override
    public AuthResponse verifyMfa(VerifyMfaRequest request) {
        String email = jwtUtil.extractEmail(request.getTempToken());
        if (request.getMfaCode() == null || request.getMfaCode().length() != 6) {
            throw new InvalidTokenException("Invalid MFA code");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        AppUserDetails userDetails = (AppUserDetails) customUserDetailsService.loadUserByUsername(email);

        return AuthResponse.builder()
                .accessToken(jwtUtil.generateToken(userDetails))
                .refreshToken(jwtUtil.generateRefreshToken(userDetails))
                .requiresMfa(false)
                .user(mapUser(user))
                .build();
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String email = jwtUtil.extractEmail(request.getRefreshToken());
        AppUserDetails userDetails = (AppUserDetails) customUserDetailsService.loadUserByUsername(email);
        if (!jwtUtil.validateToken(request.getRefreshToken(), userDetails)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        return AuthResponse.builder()
                .accessToken(jwtUtil.generateToken(userDetails))
                .build();
    }

    @Override
    @Transactional
    public MessageResponse verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        if (user.getEmailVerificationExpiry() == null || user.getEmailVerificationExpiry().isBefore(OffsetDateTime.now())) {
            throw new InvalidTokenException("Verification token has expired");
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpiry(null);
        userRepository.save(user);

        return MessageResponse.builder().message("Email verified successfully").build();
    }

    @Override
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            user.setPasswordResetToken(UUID.randomUUID().toString());
            user.setPasswordResetExpiry(OffsetDateTime.now().plusMinutes(passwordResetExpiryMinutes));
            userRepository.save(user);
        });
        return MessageResponse.builder().message("If that email exists, a reset link has been sent").build();
    }

    @Override
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidOperationException("Passwords do not match");
        }
        if (!request.getNewPassword().matches("^(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$")) {
            throw new InvalidOperationException("Password must be minimum 8 characters with at least one number and one special character");
        }

        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid reset token"));
        if (user.getPasswordResetExpiry() == null || user.getPasswordResetExpiry().isBefore(OffsetDateTime.now())) {
            throw new InvalidTokenException("Password reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        user.setIsLocked(false);
        user.setLoginAttempts(0);
        userRepository.save(user);

        return MessageResponse.builder().message("Password reset successfully").build();
    }

    @Override
    public MfaSetupResponse setupMfa(String email) {
        String secret = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        List<String> backupCodes = List.of(
                UUID.randomUUID().toString().substring(0, 8),
                UUID.randomUUID().toString().substring(0, 8),
                UUID.randomUUID().toString().substring(0, 8),
                UUID.randomUUID().toString().substring(0, 8),
                UUID.randomUUID().toString().substring(0, 8),
                UUID.randomUUID().toString().substring(0, 8),
                UUID.randomUUID().toString().substring(0, 8),
                UUID.randomUUID().toString().substring(0, 8),
                UUID.randomUUID().toString().substring(0, 8),
                UUID.randomUUID().toString().substring(0, 8)
        );
        return MfaSetupResponse.builder()
                .secret(secret)
                .qrCodeUri("otpauth://totp/WhizuppJPIMS:" + email + "?secret=" + secret + "&issuer=WhizuppJPIMS")
                .backupCodes(backupCodes)
                .build();
    }

    @Override
    @Transactional
    public MessageResponse confirmMfa(String email, ConfirmMfaRequest request) {
        if (request.getMfaCode() == null || request.getMfaCode().length() != 6) {
            throw new InvalidTokenException("Invalid MFA code");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setMfaSecret(request.getSecret());
        user.setMfaEnabled(true);
        userRepository.save(user);

        return MessageResponse.builder().message("MFA enabled successfully").build();
    }

    @Override
    @Transactional
    public MessageResponse disableMfa(String email, DisableMfaRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidOperationException("Invalid password");
        }

        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        userRepository.save(user);
        return MessageResponse.builder().message("MFA disabled successfully").build();
    }

    @Override
    public UserSummaryResponse getMe(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapUser(user);
    }

    @Override
    @Transactional
    public UserSummaryResponse updateMe(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setDepartment(request.getDepartment());
        userRepository.save(user);

        return mapUser(user);
    }

    @Override
    @Transactional
    public MessageResponse changePassword(String email, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidOperationException("Passwords do not match");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidOperationException("Current password is incorrect");
        }

        if (!request.getNewPassword().matches("^(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$")) {
            throw new InvalidOperationException("Password must be minimum 8 characters with at least one number and one special character");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return MessageResponse.builder().message("Password changed successfully").build();
    }

    @Override
    public MessageResponse logout() {
        return MessageResponse.builder().message("Logged out successfully").build();
    }

    private UserSummaryResponse mapUser(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .employeeId(user.getEmployeeId())
                .role(user.getRole())
                .department(user.getDepartment())
                .build();
    }
}
