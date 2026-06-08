package com.whizupp.jpims.repository;

import com.whizupp.jpims.entity.FinishedProduct;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinishedProductRepository extends JpaRepository<FinishedProduct, UUID> {
    List<FinishedProduct> findByExpiryDateLessThanEqual(LocalDate date);

    Page<FinishedProduct> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Optional<FinishedProduct> findByProductionBatch_Id(UUID batchId);

    boolean existsByProductionBatch_Id(UUID batchId);
}
