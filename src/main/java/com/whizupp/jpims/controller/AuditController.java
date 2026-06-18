package com.whizupp.jpims.controller;

import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.whizupp.jpims.dto.response.AuditLogResponse;
import com.whizupp.jpims.entity.AuditLog;
import com.whizupp.jpims.entity.DataRetentionPolicy;
import com.whizupp.jpims.exception.ResourceNotFoundException;
import com.whizupp.jpims.repository.DataRetentionPolicyRepository;
import com.whizupp.jpims.service.AuditService;
import lombok.RequiredArgsConstructor;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMINISTRATOR')")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;
    private final DataRetentionPolicyRepository dataRetentionPolicyRepository;

    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(auditService.listLogs(pageable).map(this::mapToResponse));
    }

    @GetMapping("/anomalies")
    public ResponseEntity<Page<AuditLogResponse>> anomalies(Pageable pageable) {
        return ResponseEntity.ok(auditService.getLogsByAnomaly(pageable).map(this::mapToResponse));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<AuditLogResponse>> userAudit(@PathVariable UUID userId, Pageable pageable) {
        return ResponseEntity.ok(auditService.getLogsByUser(userId, pageable).map(this::mapToResponse));
    }

    @GetMapping("/module/{module}")
    public ResponseEntity<Page<AuditLogResponse>> moduleAudit(@PathVariable String module, Pageable pageable) {
        return ResponseEntity.ok(auditService.getLogsByModule(module, pageable).map(this::mapToResponse));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        List<AuditLog> logs = auditService.getAllLogs();
        StringBuilder csv = new StringBuilder("Timestamp,User,Action,Module,IP Address,Details,Is Anomaly\n");
        for (AuditLog log : logs) {
            String user = log.getUser() != null ? log.getUser().getFullName() : "SYSTEM";
            String details = log.getNewValue() != null ? log.getNewValue().replace("\"", "\"\"") : "";
            csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    log.getTimestamp() != null ? log.getTimestamp().toString() : "",
                    user.replace("\"", "\"\""),
                    log.getAction() != null ? log.getAction().replace("\"", "\"\"") : "",
                    log.getModule() != null ? log.getModule().replace("\"", "\"\"") : "",
                    log.getIpAddress() != null ? log.getIpAddress().replace("\"", "\"\"") : "",
                    details,
                    log.getIsAnomaly() != null && log.getIsAnomaly() ? "Yes" : "No"
            ));
        }
        byte[] csvBytes = csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"audit_log.csv\"")
                .header("Content-Type", "text/csv")
                .body(csvBytes);
    }

    @GetMapping("/retention-policies")
    public ResponseEntity<Page<DataRetentionPolicy>> retentionPolicies(Pageable pageable) {
        return ResponseEntity.ok(dataRetentionPolicyRepository.findAll(pageable));
    }

    @PutMapping("/retention-policies/{id}")
    public ResponseEntity<DataRetentionPolicy> updateRetentionPolicy(@PathVariable UUID id, @RequestBody DataRetentionPolicy body) {
        DataRetentionPolicy policy = dataRetentionPolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found"));
        policy.setRetentionDays(body.getRetentionDays());
        policy.setArchiveEnabled(body.getArchiveEnabled());
        policy.setUpdatedAt(OffsetDateTime.now());
        return ResponseEntity.ok(dataRetentionPolicyRepository.save(policy));
    }

    private AuditLogResponse mapToResponse(AuditLog log) {
        String details = log.getNewValue();
        if (log.getOldValue() != null && !log.getOldValue().isEmpty() && log.getNewValue() != null && !log.getNewValue().isEmpty()) {
            details = "Changed from '" + log.getOldValue() + "' to '" + log.getNewValue() + "'";
        } else if (log.getNewValue() != null && !log.getNewValue().isEmpty()) {
            details = log.getNewValue();
        } else if (log.getOldValue() != null && !log.getOldValue().isEmpty()) {
            details = "Removed: " + log.getOldValue();
        } else {
            details = "Action performed";
        }

        return AuditLogResponse.builder()
                .id(log.getId())
                .userName(log.getUser() != null ? log.getUser().getFullName() : "SYSTEM")
                .action(log.getAction())
                .entity(log.getModule())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .details(details)
                .timestamp(log.getTimestamp())
                .ipAddress(log.getIpAddress())
                .isAnomaly(log.getIsAnomaly())
                .build();
    }
}
