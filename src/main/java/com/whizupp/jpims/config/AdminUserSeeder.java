package com.whizupp.jpims.config;

import com.whizupp.jpims.entity.User;
import com.whizupp.jpims.enums.DomainEnums.Role;
import com.whizupp.jpims.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.admin.full-name:Pacifique Irafasha}")
    private String fullName;

    @Value("${app.seed.admin.email:irapac40@gmail.com}")
    private String email;

    @Value("${app.seed.admin.password:irafasha}")
    private String password;

    @Value("${app.seed.admin.phone:+233500000000}")
    private String phone;

    @Value("${app.seed.admin.employee-id:ADMIN-001}")
    private String employeeId;

    @Value("${app.seed.admin.department:IT}")
    private String department;

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("Admin seed is disabled.");
            return;
        }

        if (userRepository.findByEmail(email).isPresent()) {
            log.info("Admin seed skipped: user already exists for {}", email);
            return;
        }

        User admin = User.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .phone(phone)
                .employeeId(employeeId)
                .department(department)
                .role(Role.ADMINISTRATOR)
                .isActive(true)
                .isLocked(false)
                .emailVerified(true)
                .mustChangePassword(false)
                .build();

        userRepository.save(admin);
        log.info("Default admin user created: {}", email);
    }
}
