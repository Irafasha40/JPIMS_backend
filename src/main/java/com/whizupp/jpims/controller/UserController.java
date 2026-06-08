package com.whizupp.jpims.controller;

import com.whizupp.jpims.dto.request.RegisterRequest;
import com.whizupp.jpims.dto.response.UserResponse;
import com.whizupp.jpims.entity.User;
import com.whizupp.jpims.service.UserService;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMINISTRATOR')")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserResponse>> listUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isLocked,
            Pageable pageable) {
        Page<User> users = userService.list(pageable);
        return ResponseEntity.ok(users.map(this::mapToResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(mapToResponse(userService.getUser(id)));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(201).body(mapToResponse(userService.createUser(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id, @RequestBody User body) {
        return ResponseEntity.ok(mapToResponse(userService.updateUser(id, body)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> softDelete(@PathVariable UUID id) {
        userService.softDeleteUser(id);
        return ResponseEntity.ok(Map.of("id", id, "isActive", false));
    }

    @PutMapping("/{id}/unlock")
    public ResponseEntity<Map<String, Object>> unlock(@PathVariable UUID id) {
        User user = userService.unlockUser(id);
        return ResponseEntity.ok(Map.of("id", id, "isLocked", false, "loginAttempts", user.getLoginAttempts()));
    }

    @PutMapping("/{id}/reset-password")
    public ResponseEntity<Map<String, Object>> adminResetPassword(@PathVariable UUID id) {
        String tempPassword = UUID.randomUUID().toString().substring(0, 12);
        userService.resetPassword(id, tempPassword);
        return ResponseEntity.ok(Map.of("id", id, "tempPassword", tempPassword, "message", "Temporary password generated"));
    }

    @GetMapping("/{id}/audit-trail")
    public ResponseEntity<Page<Map<String, Object>>> userAuditTrail(@PathVariable UUID id, Pageable pageable) {
        return ResponseEntity.ok(Page.empty(pageable));
    }

    @GetMapping("/{id}/sessions")
    public ResponseEntity<Map<String, Object>> userSessions(@PathVariable UUID id) {
        return ResponseEntity.ok(Map.of("id", id));
    }

    @PostMapping("/bulk-import")
    public ResponseEntity<Map<String, Object>> bulkImport() {
        return ResponseEntity.ok(Map.of("message", "Bulk import endpoint scaffolded"));
    }

    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportUsers() {
        return ResponseEntity.ok(Map.of("message", "CSV export endpoint scaffolded"));
    }

    @GetMapping("/access-requests")
    public ResponseEntity<Page<Map<String, Object>>> listAccessRequests(Pageable pageable) {
        return ResponseEntity.ok(Page.empty(pageable));
    }

    @PutMapping("/access-requests/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveAccessRequest(@PathVariable UUID id, @RequestBody(required = false) Map<String, Object> body) {
        return ResponseEntity.ok(Map.of("id", id, "status", "APPROVED"));
    }

    @PutMapping("/access-requests/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectAccessRequest(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(Map.of("id", id, "status", "REJECTED"));
    }

    @GetMapping("/permissions")
    public ResponseEntity<Page<Map<String, Object>>> permissions(Pageable pageable) {
        return ResponseEntity.ok(Page.empty(pageable));
    }

    @PutMapping("/permissions")
    public ResponseEntity<Map<String, Object>> updatePermissions(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(body);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .employeeId(user.getEmployeeId())
                .department(user.getDepartment())
                .role(user.getRole().toString())
                .isActive(user.getIsActive())
                .isLocked(user.getIsLocked())
                .lastLogin(user.getLastLogin())
                .mfaEnabled(user.getMfaEnabled())
                .emailVerified(user.getEmailVerified())
                .build();
    }
}
