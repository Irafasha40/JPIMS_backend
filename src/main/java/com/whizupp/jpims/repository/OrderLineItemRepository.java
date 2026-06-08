package com.whizupp.jpims.repository;

import com.whizupp.jpims.entity.OrderLineItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderLineItemRepository extends JpaRepository<OrderLineItem, UUID> {
    List<OrderLineItem> findBySalesOrderId(UUID salesOrderId);
}
