package com.whizupp.jpims.repository;

import com.whizupp.jpims.entity.StockMovement;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {
    @EntityGraph(attributePaths = {"rawMaterial", "recordedBy"})
    Page<StockMovement> findByRawMaterial_Id(UUID rawMaterialId, Pageable pageable);
}
