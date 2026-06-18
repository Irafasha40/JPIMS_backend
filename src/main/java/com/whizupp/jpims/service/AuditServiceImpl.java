package com.whizupp.jpims.service;

import com.whizupp.jpims.entity.AuditLog;
import com.whizupp.jpims.entity.User;
import com.whizupp.jpims.repository.AuditLogRepository;
import com.whizupp.jpims.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final HttpServletRequest request;

    @Override
    @Transactional
    public void log(String action, String module, String recordId, String oldValue, String newValue, boolean isAnomaly) {
        String email = "SYSTEM";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            email = auth.getName();
        }

        User user = null;
        if (!"SYSTEM".equals(email)) {
            user = userRepository.findByEmail(email).orElse(null);
        }

        String ip = "127.0.0.1";
        if (request != null) {
            String xff = request.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isEmpty()) {
                ip = xff.split(",")[0].trim();
            } else {
                ip = request.getRemoteAddr();
            }
        }

        AuditLog logEntry = AuditLog.builder()
                .user(user)
                .action(action)
                .module(module)
                .recordId(recordId)
                .oldValue(oldValue)
                .newValue(newValue)
                .timestamp(OffsetDateTime.now())
                .ipAddress(ip)
                .isAnomaly(isAnomaly)
                .build();

        auditLogRepository.save(logEntry);
        log.info("Audit logged: action={}, module={}, user={}, ip={}, anomaly={}", action, module, email, ip, isAnomaly);
    }

    @Override
    @Transactional
    public void log(String action, String module, String recordId, String oldValue, String newValue) {
        log(action, module, recordId, oldValue, newValue, false);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> listLogs(Pageable pageable) {
        return auditLogRepository.findAll(ensureDescSorted(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getLogsByModule(String module, Pageable pageable) {
        Pageable sorted = ensureDescSorted(pageable);
        String upper = module.toUpperCase();
        if ("PRODUCTION".equals(upper)) {
            return auditLogRepository.findByModuleIn(List.of("PRODUCTION", "PRODUCTION_BATCH_MANAGEMENT", "RECIPE_FORMULATION"), sorted);
        } else if ("QUALITY".equals(upper) || "QUALITY_CONTROL".equals(upper)) {
            return auditLogRepository.findByModuleIn(List.of("QUALITY_CONTROL", "QUALITY"), sorted);
        } else if ("SALES".equals(upper)) {
            return auditLogRepository.findByModuleIn(List.of("SALES_ORDER_MANAGEMENT", "SALES", "CUSTOMER_MANAGEMENT"), sorted);
        } else if ("INVENTORY".equals(upper) || "RAW_MATERIAL".equals(upper)) {
            return auditLogRepository.findByModuleIn(List.of("RAW_MATERIAL_INVENTORY", "FINISHED_PRODUCT_INVENTORY", "SUPPLIER_MANAGEMENT"), sorted);
        } else {
            return auditLogRepository.findByModuleIgnoreCase(module, sorted);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getLogsByAnomaly(Pageable pageable) {
        return auditLogRepository.findByIsAnomalyTrue(ensureDescSorted(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getLogsByUser(UUID userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, ensureDescSorted(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
    }

    private Pageable ensureDescSorted(Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "timestamp"));
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedLogs() {
        if (auditLogRepository.count() == 0) {
            log.info("Seeding historical audit logs for demonstration...");
            User admin = userRepository.findByEmail("admin@whizupp.com").orElse(null);

            OffsetDateTime now = OffsetDateTime.now();

            // 1. Initial System Setup / Seeding logs
            auditLogRepository.save(AuditLog.builder()
                    .user(null)
                    .action("system_startup")
                    .module("AUTHENTICATION")
                    .newValue("JPIMS Application Server Started")
                    .timestamp(now.minusHours(48))
                    .ipAddress("127.0.0.1")
                    .isAnomaly(false)
                    .build());

            // 2. Admin User Registrations/logins
            auditLogRepository.save(AuditLog.builder()
                    .user(admin)
                    .action("login")
                    .module("AUTHENTICATION")
                    .newValue("Successful login for admin@whizupp.com")
                    .timestamp(now.minusHours(47))
                    .ipAddress("192.168.1.15")
                    .isAnomaly(false)
                    .build());

            // 3. User CRUD (e.g. creating raw materials and recipes)
            auditLogRepository.save(AuditLog.builder()
                    .user(admin)
                    .action("create")
                    .module("RAW_MATERIAL_INVENTORY")
                    .recordId(UUID.randomUUID().toString())
                    .newValue("Created Raw Material: Orange Concentrate")
                    .timestamp(now.minusHours(46))
                    .ipAddress("192.168.1.15")
                    .isAnomaly(false)
                    .build());

            auditLogRepository.save(AuditLog.builder()
                    .user(admin)
                    .action("create")
                    .module("RECIPE_FORMULATION")
                    .recordId(UUID.randomUUID().toString())
                    .newValue("Created Recipe: Sweet Orange Juice Premium v1.0")
                    .timestamp(now.minusHours(45))
                    .ipAddress("192.168.1.15")
                    .isAnomaly(false)
                    .build());

            // 4. Anomaly events (e.g. Failed logins)
            auditLogRepository.save(AuditLog.builder()
                    .user(null)
                    .action("login_failed")
                    .module("AUTHENTICATION")
                    .newValue("Failed login attempt for unknown user: attacker@evil.com")
                    .timestamp(now.minusHours(40))
                    .ipAddress("185.220.101.5")
                    .isAnomaly(true)
                    .build());

            auditLogRepository.save(AuditLog.builder()
                    .user(null)
                    .action("login_failed")
                    .module("AUTHENTICATION")
                    .newValue("Failed login attempt for user: admin@whizupp.com (Invalid Password)")
                    .timestamp(now.minusHours(39))
                    .ipAddress("185.220.101.5")
                    .isAnomaly(true)
                    .build());

            auditLogRepository.save(AuditLog.builder()
                    .user(null)
                    .action("login_failed")
                    .module("AUTHENTICATION")
                    .newValue("Failed login attempt for user: admin@whizupp.com (Invalid Password)")
                    .timestamp(now.minusHours(38))
                    .ipAddress("185.220.101.5")
                    .isAnomaly(true)
                    .build());

            // 5. Production events
            auditLogRepository.save(AuditLog.builder()
                    .user(admin)
                    .action("create")
                    .module("PRODUCTION_BATCH_MANAGEMENT")
                    .recordId(UUID.randomUUID().toString())
                    .newValue("Created Production Batch: BATCH-2026-001 for Sweet Orange Juice")
                    .timestamp(now.minusHours(24))
                    .ipAddress("192.168.1.22")
                    .isAnomaly(false)
                    .build());

            auditLogRepository.save(AuditLog.builder()
                    .user(admin)
                    .action("update")
                    .module("PRODUCTION_BATCH_MANAGEMENT")
                    .recordId(UUID.randomUUID().toString())
                    .oldValue("PLANNED")
                    .newValue("IN_PROGRESS")
                    .timestamp(now.minusHours(23))
                    .ipAddress("192.168.1.22")
                    .isAnomaly(false)
                    .build());

            // 6. Quality Control events
            auditLogRepository.save(AuditLog.builder()
                    .user(admin)
                    .action("create")
                    .module("QUALITY_CONTROL")
                    .recordId(UUID.randomUUID().toString())
                    .newValue("Created Quality Test: Sweet Orange Juice BATCH-2026-001 (Result: PASSED)")
                    .timestamp(now.minusHours(20))
                    .ipAddress("192.168.1.24")
                    .isAnomaly(false)
                    .build());

            // 7. Sales events
            auditLogRepository.save(AuditLog.builder()
                    .user(admin)
                    .action("create")
                    .module("SALES_ORDER_MANAGEMENT")
                    .recordId(UUID.randomUUID().toString())
                    .newValue("Created Sales Order SO-2026-009 for Juice Distributors Inc.")
                    .timestamp(now.minusHours(10))
                    .ipAddress("192.168.1.15")
                    .isAnomaly(false)
                    .build());

            // 8. Policy updates
            auditLogRepository.save(AuditLog.builder()
                    .user(admin)
                    .action("update")
                    .module("SECURITY_AUDIT")
                    .newValue("Updated Retention Policy for module AUTHENTICATION to 90 days")
                    .timestamp(now.minusHours(5))
                    .ipAddress("192.168.1.15")
                    .isAnomaly(false)
                    .build());

            log.info("Audit log seeding completed successfully.");
        }
    }
}
