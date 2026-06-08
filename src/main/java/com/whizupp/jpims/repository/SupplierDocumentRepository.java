package com.whizupp.jpims.repository;

import com.whizupp.jpims.entity.SupplierDocument;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierDocumentRepository extends JpaRepository<SupplierDocument, UUID> {
}
