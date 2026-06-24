package com.whizupp.jpims.controller;

import com.whizupp.jpims.dto.response.NotificationResponse;
import com.whizupp.jpims.entity.Notification;
import com.whizupp.jpims.entity.User;
import com.whizupp.jpims.enums.DomainEnums.Role;
import com.whizupp.jpims.exception.ResourceNotFoundException;
import com.whizupp.jpims.repository.NotificationRepository;
import com.whizupp.jpims.repository.UserRepository;
import com.whizupp.jpims.service.NotificationService;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> list(Authentication authentication, Pageable pageable) {
        User recipient = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(notificationRepository
                .findByRecipientOrderByCreatedAtDesc(recipient, pageable)
                .map(this::toResponse));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> unreadCount(Authentication authentication) {
        return ResponseEntity.ok(Map.of("count", notificationRepository.countByRecipientAndIsReadFalse(getAuthenticatedUser(authentication))));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markRead(@PathVariable UUID id, Authentication authentication) {
        Notification notification = notificationRepository
                .findByIdAndRecipient(id, getAuthenticatedUser(authentication))
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setIsRead(true);
        return ResponseEntity.ok(toResponse(notificationRepository.save(notification)));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllRead(Authentication authentication) {
        int marked = notificationRepository.markAllRead(getAuthenticatedUser(authentication));
        return ResponseEntity.ok(Map.of("marked", marked));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<NotificationResponse>> history(Authentication authentication, Pageable pageable) {
        return list(authentication, pageable);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
        notificationRepository.deleteByIdAndRecipient(id, getAuthenticatedUser(authentication));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/preferences")
    public ResponseEntity<Page<Map<String, Object>>> preferences(Pageable pageable) { return ResponseEntity.ok(Page.empty(pageable)); }

    @PutMapping("/preferences")
    public ResponseEntity<Map<String, Object>> updatePreferences(@RequestBody Map<String, Object> body) { return ResponseEntity.ok(body); }

    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> broadcast(@RequestBody Map<String, Object> body) {
        String title = String.valueOf(body.getOrDefault("title", "Notification"));
        String message = String.valueOf(body.getOrDefault("message", ""));
        String recipient = String.valueOf(body.getOrDefault("recipient", "all")).toLowerCase();
        if ("all".equals(recipient)) {
            notificationService.broadcastToAll(title, message);
        } else {
            notificationService.broadcastToRole(title, message, Role.valueOf(recipient.toUpperCase()));
        }
        return ResponseEntity.ok(Map.of("message", "Broadcast sent"));
    }

    @GetMapping("/reports/scheduled")
    public ResponseEntity<Page<Map<String, Object>>> reportSchedules(Pageable pageable) { return ResponseEntity.ok(Page.empty(pageable)); }

    @PostMapping("/reports/scheduled")
    public ResponseEntity<Map<String, Object>> scheduleReport(@RequestBody Map<String, Object> body) { return ResponseEntity.status(201).body(body); }

    @PutMapping("/reports/scheduled/{id}")
    public ResponseEntity<Map<String, Object>> updateSchedule(@PathVariable UUID id, @RequestBody Map<String, Object> body) { return ResponseEntity.ok(body); }

    @DeleteMapping("/reports/scheduled/{id}")
    public ResponseEntity<Map<String, Object>> removeSchedule(@PathVariable UUID id) { return ResponseEntity.ok(Map.of("id", id)); }

    private User getAuthenticatedUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType() != null ? notification.getType().name() : null)
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
