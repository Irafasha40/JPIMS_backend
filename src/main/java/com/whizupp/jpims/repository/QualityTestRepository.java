package com.whizupp.jpims.repository;

import com.whizupp.jpims.entity.QualityTest;
import com.whizupp.jpims.enums.DomainEnums.TestResult;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QualityTestRepository extends JpaRepository<QualityTest, UUID> {
    List<QualityTest> findByProductionBatchId(UUID batchId);
    boolean existsByProductionBatchIdAndResult(UUID batchId, TestResult result);
    long countByResult(TestResult result);
}
