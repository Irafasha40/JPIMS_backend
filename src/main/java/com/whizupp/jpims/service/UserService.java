package com.whizupp.jpims.service;

import com.whizupp.jpims.dto.request.RegisterRequest;
import com.whizupp.jpims.entity.User;
import com.whizupp.jpims.exception.InvalidOperationException;
import com.whizupp.jpims.exception.ResourceNotFoundException;
import com.whizupp.jpims.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public Page<User> list(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional
    public User createUser(RegisterRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new InvalidOperationException("Email already exists");
        });
        User saved = userRepository.save(User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .employeeId(request.getEmployeeId())
                .department(request.getDepartment())
                .role(request.getRole())
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .isLocked(false)
                .emailVerified(true)
                .mustChangePassword(true)
                .build());
        auditService.log("create", "USER_MANAGEMENT", saved.getId().toString(), null, "Created user: " + saved.getEmail());
        return saved;
    }

    public User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public User updateUser(UUID id, User request) {
        User user = getUser(id);
        String oldDetails = String.format("Name: %s, Dept: %s", user.getFullName(), user.getDepartment());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setDepartment(request.getDepartment());
        User saved = userRepository.save(user);
        String newDetails = String.format("Name: %s, Dept: %s", saved.getFullName(), saved.getDepartment());
        auditService.log("update", "USER_MANAGEMENT", saved.getId().toString(), oldDetails, "Updated user: " + newDetails);
        return saved;
    }

    @Transactional
    public void softDeleteUser(UUID id) {
        User user = getUser(id);
        user.setIsActive(false);
        userRepository.save(user);
        auditService.log("delete", "USER_MANAGEMENT", user.getId().toString(), "ACTIVE", "Deactivated user: " + user.getEmail());
    }

    @Transactional
    public User unlockUser(UUID id) {
        User user = getUser(id);
        user.setIsLocked(false);
        user.setLoginAttempts(0);
        User saved = userRepository.save(user);
        auditService.log("unlock", "USER_MANAGEMENT", saved.getId().toString(), "LOCKED", "Unlocked user account: " + saved.getEmail());
        return saved;
    }

    @Transactional
    public User resetPassword(UUID id, String tempPassword) {
        User user = getUser(id);
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setMustChangePassword(true);
        User saved = userRepository.save(user);
        auditService.log("reset_password", "USER_MANAGEMENT", saved.getId().toString(), null, "Admin reset password for user: " + saved.getEmail());
        return saved;
    }
}
