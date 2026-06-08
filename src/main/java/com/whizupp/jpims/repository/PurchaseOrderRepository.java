package com.whizupp.jpims.repository;

import com.whizupp.jpims.entity.PurchaseOrder;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
    Optional<PurchaseOrder> findTopByPoNumberStartingWithOrderByPoNumberDesc(String prefix);

    /** Supplier is eager-fetched so list/detail mapping never touches an uninitialized proxy. */
    @EntityGraph(attributePaths = {"supplier"})
    @Override
    Page<PurchaseOrder> findAll(Pageable pageable);
}
