package com.whizupp.jpims.service;

import com.whizupp.jpims.dto.request.RegisterRequest;
import com.whizupp.jpims.entity.User;
import com.whizupp.jpims.exception.InvalidOperationException;
import com.whizupp.jpims.exception.ResourceNotFoundException;
import com.whizupp.jpims.entity.PermissionMatrix;
import com.whizupp.jpims.repository.PermissionMatrixRepository;
import com.whizupp.jpims.repository.UserRepository;
import com.whizupp.jpims.enums.DomainEnums.Role;
import java.util.List;
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
    private final PermissionMatrixRepository permissionMatrixRepository;

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
        String oldDetails = String.format("Name: %s, Dept: %s, Role: %s", user.getFullName(), user.getDepartment(), user.getRole());
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getDepartment() != null) user.setDepartment(request.getDepartment());
        if (request.getRole() != null) user.setRole(request.getRole());
        User saved = userRepository.save(user);
        String newDetails = String.format("Name: %s, Dept: %s, Role: %s", saved.getFullName(), saved.getDepartment(), saved.getRole());
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
    public User activateUser(UUID id) {
        User user = getUser(id);
        user.setIsActive(true);
        User saved = userRepository.save(user);
        auditService.log("activate", "USER_MANAGEMENT", saved.getId().toString(), "INACTIVE", "Activated user: " + saved.getEmail());
        return saved;
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

    @Transactional
    public List<PermissionMatrix> getPermissions() {
        List<PermissionMatrix> list = permissionMatrixRepository.findAll();
        if (list.isEmpty()) {
            List<String> modules = List.of(
                "Dashboard", "Raw Materials", "Production", "Quality Control", 
                "Finished Products", "Sales", "Recipes", "Suppliers", 
                "Reports", "Notifications", "Users", "Security"
            );
            for (Role role : Role.values()) {
                for (String mod : modules) {
                    boolean canAccess = hasDefaultAccess(role, mod);
                    PermissionMatrix pm = PermissionMatrix.builder()
                            .role(role)
                            .module(mod)
                            .canView(canAccess)
                            .canCreate(canAccess)
                            .canEdit(canAccess)
                            .canDelete(canAccess)
                            .canExport(canAccess)
                            .build();
                    permissionMatrixRepository.save(pm);
                }
            }
            list = permissionMatrixRepository.findAll();
        }
        return list;
    }

    private boolean hasDefaultAccess(Role role, String module) {
        if (role == Role.ADMINISTRATOR) {
            return true;
        }
        if ("Dashboard".equals(module)) {
            return true;
        }
        switch (role) {
            case PRODUCTION_MANAGER:
                return "Raw Materials".equals(module) || "Production".equals(module) || "Recipes".equals(module) || "Reports".equals(module) || "Notifications".equals(module);
            case INVENTORY_MANAGER:
                return "Raw Materials".equals(module) || "Finished Products".equals(module) || "Suppliers".equals(module) || "Reports".equals(module) || "Notifications".equals(module);
            case QC_OFFICER:
                return "Quality Control".equals(module) || "Production".equals(module) || "Reports".equals(module) || "Notifications".equals(module);
            case SALES_STAFF:
                return "Sales".equals(module) || "Finished Products".equals(module) || "Notifications".equals(module) || "Reports".equals(module);
            default:
                return false;
        }
    }

    @Transactional
    public void updatePermissions(List<PermissionMatrix> requestList) {
        List<PermissionMatrix> existingList = permissionMatrixRepository.findAll();
        for (PermissionMatrix request : requestList) {
            PermissionMatrix match = existingList.stream()
                .filter(pm -> pm.getRole() == request.getRole() && pm.getModule().equalsIgnoreCase(request.getModule()))
                .findFirst()
                .orElse(null);
            
            if (match != null) {
                match.setCanView(request.getCanView());
                match.setCanCreate(request.getCanView());
                match.setCanEdit(request.getCanView());
                match.setCanDelete(request.getCanView());
                match.setCanExport(request.getCanView());
                permissionMatrixRepository.save(match);
            } else {
                PermissionMatrix pm = PermissionMatrix.builder()
                        .role(request.getRole())
                        .module(request.getModule())
                        .canView(request.getCanView())
                        .canCreate(request.getCanView())
                        .canEdit(request.getCanView())
                        .canDelete(request.getCanView())
                        .canExport(request.getCanView())
                        .build();
                permissionMatrixRepository.save(pm);
            }
        }
        auditService.log("update_permissions", "USER_MANAGEMENT", null, null, "Updated role permission matrix");
    }
}
