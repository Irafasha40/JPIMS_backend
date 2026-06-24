package com.whizupp.jpims.repository;

import com.whizupp.jpims.entity.ProductionBatch;
import com.whizupp.jpims.enums.DomainEnums.BatchStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductionBatchRepository extends JpaRepository<ProductionBatch, UUID> {
    Optional<ProductionBatch> findTopByBatchNumberStartingWithOrderByBatchNumberDesc(String prefix);
    long countByStatus(BatchStatus status);
    long countByRecipeId(UUID recipeId);

    @Query("""
            SELECT b FROM ProductionBatch b
            WHERE b.status = :status
            AND NOT EXISTS (
                SELECT 1 FROM FinishedProduct fp WHERE fp.productionBatch.id = b.id
            )
            """)
    List<ProductionBatch> findCompletedWithoutFinishedProduct(@Param("status") BatchStatus status);
}
