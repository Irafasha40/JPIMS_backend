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

    public Page<User> list(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional
    public User createUser(RegisterRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new InvalidOperationException("Email already exists");
        });
        return userRepository.save(User.builder()
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
    }

    public User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public User updateUser(UUID id, User request) {
        User user = getUser(id);
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setDepartment(request.getDepartment());
        return userRepository.save(user);
    }

    @Transactional
    public void softDeleteUser(UUID id) {
        User user = getUser(id);
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Transactional
    public User unlockUser(UUID id) {
        User user = getUser(id);
        user.setIsLocked(false);
        user.setLoginAttempts(0);
        return userRepository.save(user);
    }

    @Transactional
    public User resetPassword(UUID id, String tempPassword) {
        User user = getUser(id);
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setMustChangePassword(true);
        return userRepository.save(user);
    }
}
