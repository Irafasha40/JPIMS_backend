package com.whizupp.jpims.repository;

import com.whizupp.jpims.entity.SalesOrder;
import com.whizupp.jpims.enums.DomainEnums.OrderStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, UUID> {
    Optional<SalesOrder> findTopByOrderNumberStartingWithOrderByOrderNumberDesc(String prefix);
    long countByStatus(OrderStatus status);
}
