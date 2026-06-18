package com.whizupp.jpims.service;

import com.whizupp.jpims.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface AuditService {
    void log(String action, String module, String recordId, String oldValue, String newValue, boolean isAnomaly);
    void log(String action, String module, String recordId, String oldValue, String newValue);
    Page<AuditLog> listLogs(Pageable pageable);
    Page<AuditLog> getLogsByModule(String module, Pageable pageable);
    Page<AuditLog> getLogsByAnomaly(Pageable pageable);
    Page<AuditLog> getLogsByUser(UUID userId, Pageable pageable);
    List<AuditLog> getAllLogs();
}
