package com.whizupp.jpims.repository;

import com.whizupp.jpims.entity.Supplier;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
}
