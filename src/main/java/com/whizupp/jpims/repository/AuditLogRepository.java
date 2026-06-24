package com.whizupp.jpims.repository;

import com.whizupp.jpims.entity.AuditLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    Page<AuditLog> findByModuleIn(List<String> modules, Pageable pageable);
    Page<AuditLog> findByModuleIgnoreCase(String module, Pageable pageable);
    @Query("SELECT a FROM AuditLog a WHERE a.isAnomaly = true")
    Page<AuditLog> findByIsAnomalyTrue(Pageable pageable);
    Page<AuditLog> findByUserId(UUID userId, Pageable pageable);
}
