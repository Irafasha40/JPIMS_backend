package com.whizupp.jpims.repository;

import com.whizupp.jpims.entity.BatchIngredient;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BatchIngredientRepository extends JpaRepository<BatchIngredient, UUID> {

    @Query("SELECT bi FROM BatchIngredient bi WHERE bi.productionBatch.id = :batchId")
    List<BatchIngredient> findByProductionBatchId(@Param("batchId") UUID batchId);

    @Query("SELECT bi FROM BatchIngredient bi JOIN FETCH bi.rawMaterial WHERE bi.productionBatch.id = :batchId")
    List<BatchIngredient> findByProductionBatchIdWithMaterial(@Param("batchId") UUID batchId);
}
