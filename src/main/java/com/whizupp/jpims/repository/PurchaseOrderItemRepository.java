package com.whizupp.jpims.repository;

import com.whizupp.jpims.entity.PurchaseOrderItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, UUID> {
    List<PurchaseOrderItem> findByPurchaseOrder_Id(UUID purchaseOrderId);

    @Query("SELECT DISTINCT i FROM PurchaseOrderItem i JOIN FETCH i.rawMaterial WHERE i.purchaseOrder.id = :poId")
    List<PurchaseOrderItem> findByPurchaseOrderIdWithMaterial(@Param("poId") UUID poId);
}
