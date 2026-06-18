package com.whizupp.jpims.repository;

import com.whizupp.jpims.entity.AuditLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    Page<AuditLog> findByModuleIn(List<String> modules, Pageable pageable);
    Page<AuditLog> findByModuleIgnoreCase(String module, Pageable pageable);
    Page<AuditLog> findByIsAnomalyTrue(Pageable pageable);
    Page<AuditLog> findByUserId(UUID userId, Pageable pageable);
}
