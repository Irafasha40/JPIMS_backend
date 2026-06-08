package com.whizupp.jpims.repository;

import com.whizupp.jpims.entity.FinishedProductMovement;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinishedProductMovementRepository extends JpaRepository<FinishedProductMovement, UUID> {
    Page<FinishedProductMovement> findByFinishedProductId(UUID finishedProductId, Pageable pageable);
}
