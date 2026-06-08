package com.whizupp.jpims.repository;

import com.whizupp.jpims.entity.User;
import com.whizupp.jpims.enums.DomainEnums.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailVerificationToken(String token);

    Optional<User> findByPasswordResetToken(String token);

    List<User> findByRoleAndIsActiveTrue(Role role);

    List<User> findByIsActiveTrue();
}
