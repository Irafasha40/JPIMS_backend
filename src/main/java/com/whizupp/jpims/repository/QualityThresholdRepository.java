package com.whizupp.jpims.repository;

import com.whizupp.jpims.entity.QualityThreshold;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QualityThresholdRepository extends JpaRepository<QualityThreshold, UUID> {
    Optional<QualityThreshold> findByProductName(String productName);
    Optional<QualityThreshold> findByIsDefaultTrue();
}
