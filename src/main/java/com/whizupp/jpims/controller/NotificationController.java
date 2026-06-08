package com.whizupp.jpims.controller;

import java.util.Map;
import java.util.UUID;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {
    @GetMapping
    public ResponseEntity<Page<Map<String, Object>>> list(Pageable pageable) { return ResponseEntity.ok(Page.empty(pageable)); }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> unreadCount() { return ResponseEntity.ok(Map.of("count", 0)); }

    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markRead(@PathVariable UUID id) { return ResponseEntity.ok(Map.of("id", id, "isRead", true)); }

    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllRead() { return ResponseEntity.ok(Map.of("message", "All notifications marked as read")); }

    @GetMapping("/history")
    public ResponseEntity<Page<Map<String, Object>>> history(Pageable pageable) { return ResponseEntity.ok(Page.empty(pageable)); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable UUID id) { return ResponseEntity.ok(Map.of("id", id)); }

    @GetMapping("/preferences")
    public ResponseEntity<Page<Map<String, Object>>> preferences(Pageable pageable) { return ResponseEntity.ok(Page.empty(pageable)); }

    @PutMapping("/preferences")
    public ResponseEntity<Map<String, Object>> updatePreferences(@RequestBody Map<String, Object> body) { return ResponseEntity.ok(body); }

    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> broadcast(@RequestBody Map<String, Object> body) { return ResponseEntity.ok(body); }

    @GetMapping("/reports/scheduled")
    public ResponseEntity<Page<Map<String, Object>>> reportSchedules(Pageable pageable) { return ResponseEntity.ok(Page.empty(pageable)); }

    @PostMapping("/reports/scheduled")
    public ResponseEntity<Map<String, Object>> scheduleReport(@RequestBody Map<String, Object> body) { return ResponseEntity.status(201).body(body); }

    @PutMapping("/reports/scheduled/{id}")
    public ResponseEntity<Map<String, Object>> updateSchedule(@PathVariable UUID id, @RequestBody Map<String, Object> body) { return ResponseEntity.ok(body); }

    @DeleteMapping("/reports/scheduled/{id}")
    public ResponseEntity<Map<String, Object>> removeSchedule(@PathVariable UUID id) { return ResponseEntity.ok(Map.of("id", id)); }
}
