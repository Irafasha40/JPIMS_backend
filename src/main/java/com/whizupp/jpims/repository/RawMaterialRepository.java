package com.whizupp.jpims.repository;

import com.whizupp.jpims.entity.RawMaterial;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RawMaterialRepository extends JpaRepository<RawMaterial, UUID> {
    @EntityGraph(attributePaths = {"supplier"})
    @Override
    Page<RawMaterial> findAll(Pageable pageable);

    @Query("select r from RawMaterial r where r.currentStock <= r.minimumThreshold")
    List<RawMaterial> findLowStockMaterials();

    Optional<RawMaterial> findFirstByNameIgnoreCase(String name);
}
